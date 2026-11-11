package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.constant.enums.ModuleType;
import org.jeecg.common.constant.enums.OperateTypeEnum;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * Nhật ký hệ thống, lớp xử lý aspect
 *
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2018 năm 1 tháng 14 ngày
 */
@Aspect
@Component
public class AutoLogAspect {

    @Resource
    private BaseCommonService baseCommonService;

    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.AutoLog)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //Thực thi phương thức
        Object result = point.proceed();
        //Thời gian thực thi(Mili giây)
        long time = System.currentTimeMillis() - beginTime;

        //Lưu nhật ký
        saveSysLog(point, time, result);

        return result;
    }

    private void saveSysLog(ProceedingJoinPoint joinPoint, long time, Object obj) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        LogDTO dto = new LogDTO();
        AutoLog syslog = method.getAnnotation(AutoLog.class);
        if(syslog != null){
            //update-begin-author:taoyan date:
            String content = syslog.value();
            if(syslog.module()== ModuleType.ONLINE){
                content = getOnlineLogContent(obj, content);
            }
            //Mô tả trên annotation,Nội dung nhật ký thao tác
            dto.setLogType(syslog.logType());
            dto.setLogContent(content);
        }

        //Tên phương thức được yêu cầu
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        dto.setMethod(className + "." + methodName + "()");


        //Thiết lập loại thao tác
        if (CommonConstant.LOG_TYPE_2 == dto.getLogType()) {
            dto.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        //Lấy request
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        //Tham số của yêu cầu
        dto.setRequestParam(getReqestParams(request,joinPoint));
        //Thiết lập địa chỉ IP
        dto.setIp(IpUtils.getIpAddr(request));
        //Thiết lập client
        if (BrowserUtils.isDesktop(request)) {
            dto.setClientType(ClientTerminalTypeEnum.PC.getKey());
        } else {
            dto.setClientType(ClientTerminalTypeEnum.APP.getKey());
        }
        //Lấy thông tin người dùng đăng nhập
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(sysUser!=null){
            dto.setUserid(sysUser.getUsername());
            dto.setUsername(sysUser.getRealname());

        }
        //Thời gian tốn
        dto.setCostTime(time);
        dto.setCreateTime(new Date());
        //Lưu nhật ký hệ thống
        baseCommonService.addLog(dto);
    }


    /**
     * Lấy loại thao tác
     */
    private int getOperateType(String methodName,int operateType) {
        if (operateType > 0) {
            return operateType;
        }
        // Giải thích logic mã: Quy chuẩn quét mã Alibaba Cloud(Không cho phép bất kỳ magic value nào xuất hiện trong mã)------------
        return OperateTypeEnum.getTypeByMethodName(methodName);
    }

    /**
     * @Description: Lấy tham số yêu cầu
     * @author: scott
     * @date: 2020/4/16 0:10
     * @param request:  request
     * @param joinPoint:  joinPoint
     * @Return: java.lang.String
     */
    private String getReqestParams(HttpServletRequest request, JoinPoint joinPoint) {
        String httpMethod = request.getMethod();
        String params = "";
        if (CommonConstant.HTTP_POST.equals(httpMethod) || CommonConstant.HTTP_PUT.equals(httpMethod) || CommonConstant.HTTP_PATCH.equals(httpMethod)) {
            Object[] paramsArray = joinPoint.getArgs();
            // java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
            //  https://my.oschina.net/mengzhang6/blog/2395893
            Object[] arguments  = new Object[paramsArray.length];
            for (int i = 0; i < paramsArray.length; i++) {
                if (paramsArray[i] instanceof BindingResult || paramsArray[i] instanceof ServletRequest || paramsArray[i] instanceof ServletResponse || paramsArray[i] instanceof MultipartFile || paramsArray[i] instanceof MultipartFile[]) {
                    //ServletRequest Không thể serialize, loại khỏi tham số đầu vào, nếu không sẽ báo ngoại lệ：java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
                    //ServletResponse Không thể serialize, loại khỏi tham số đầu vào, nếu không sẽ báo ngoại lệ：java.lang.IllegalStateException: getOutputStream() has already been called for this response
                    //MultipartFile và MultipartFile[]Không thể serialize, loại khỏi tham số đầu vào
                    continue;
                }
                arguments[i] = paramsArray[i];
            }
            // Giải thích logic mã: Dữ liệu nhật ký quá dài thì lọc trực tiếp
            PropertyFilter profilter = new PropertyFilter() {
                @Override
                public boolean apply(Object o, String name, Object value) {
                    int length = 500;
                    if(value!=null && value.toString().length()>length){
                        return false;
                    }
                    if(value instanceof MultipartFile){
                        return false;
                    }
                    return true;
                }
            };
            params = JSONObject.toJSONString(arguments, profilter);
        } else {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            // Giá trị tham số của phương thức được yêu cầu
            Object[] args = joinPoint.getArgs();
            // Tên tham số của phương thức được yêu cầu
            StandardReflectionParameterNameDiscoverer u= new StandardReflectionParameterNameDiscoverer();
            String[] paramNames = u.getParameterNames(method);
            if (args != null && paramNames != null) {
                for (int i = 0; i < args.length; i++) {
                    params += "  " + paramNames[i] + ": " + args[i];
                }
            }
        }
        return params;
    }

    /**
     * online Nối nội dung nhật ký
     * @param obj
     * @param content
     * @return
     */
    private String getOnlineLogContent(Object obj, String content){
        if (Result.class.isInstance(obj)){
            Result res = (Result)obj;
            String msg = res.getMessage();
            String tableName = res.getOnlTable();
            if(oConvertUtils.isNotEmpty(tableName)){
                content+=",Tên bảng:"+tableName;
            }
            if(res.isSuccess()){
                content+= ","+(oConvertUtils.isEmpty(msg)?"Thao tác thành công":msg);
            }else{
                content+= ","+(oConvertUtils.isEmpty(msg)?"Thao tác thất bại":msg);
            }
        }
        return content;
    }


    /*    private void saveSysLog(ProceedingJoinPoint joinPoint, long time, Object obj) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SysLog sysLog = new SysLog();
        AutoLog syslog = method.getAnnotation(AutoLog.class);
        if(syslog != null){
            //update-begin-author:taoyan date:
            String content = syslog.value();
            if(syslog.module()== ModuleType.ONLINE){
                content = getOnlineLogContent(obj, content);
            }
            //Mô tả trên annotation,Nội dung nhật ký thao tác
            sysLog.setLogContent(content);
            sysLog.setLogType(syslog.logType());
        }

        //Tên phương thức được yêu cầu
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");


        //Thiết lập loại thao tác
        if (sysLog.getLogType() == CommonConstant.LOG_TYPE_2) {
            sysLog.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        //Lấy request
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        //Tham số của yêu cầu
        sysLog.setRequestParam(getReqestParams(request,joinPoint));

        //Thiết lập địa chỉ IP
        sysLog.setIp(IPUtils.getIpAddr(request));

        //Lấy thông tin người dùng đăng nhập
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        if(sysUser!=null){
            sysLog.setUserid(sysUser.getUsername());
            sysLog.setUsername(sysUser.getRealname());

        }
        //Thời gian tốn
        sysLog.setCostTime(time);
        sysLog.setCreateTime(new Date());
        //Lưu nhật ký hệ thống
        sysLogService.save(sysLog);
    }*/
}
