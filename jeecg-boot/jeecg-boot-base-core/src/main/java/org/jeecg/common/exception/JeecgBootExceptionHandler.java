package org.jeecg.common.exception;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.enums.SentinelErrorInfoEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.springframework.beans.BeansException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bộ xử lý ngoại lệ
 * 
 * @Author scott
 * @Date 2019
 */
@RestControllerAdvice
@Slf4j
public class JeecgBootExceptionHandler {

	@Resource
	BaseCommonService baseCommonService;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<?> handleValidationExceptions(MethodArgumentNotValidException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("Kiểm chứng thất bại!" + e.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(",")));
	}
	
	/**
	 * Xử lý ngoại lệ tùy chỉnh
	 */
	@ExceptionHandler(JeecgBootException.class)
	public Result<?> handleJeecgBootException(JeecgBootException e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(e.getErrCode(), e.getMessage());
	}
	
	/**
	 * Xử lý ngoại lệ tùy chỉnh
	 */
	@ExceptionHandler(JeecgBootBizTipException.class)
	public Result<?> handleJeecgBootBizTipException(JeecgBootBizTipException e){
		log.error(e.getMessage());
		return Result.error(e.getErrCode(), e.getMessage());
	}

	/**
	 * Xử lý ngoại lệ microservice tùy chỉnh
	 */
	@ExceptionHandler(JeecgCloudException.class)
	public Result<?> handleJeecgCloudException(JeecgCloudException e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(e.getMessage());
	}

	/**
	 * Xử lý ngoại lệ tùy chỉnh
	 */
	@ExceptionHandler(JeecgBoot401Exception.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Result<?> handleJeecgBoot401Exception(JeecgBoot401Exception e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return new Result(401,e.getMessage());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public Result<?> handlerNoFoundException(Exception e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(404, "Đường dẫn không tồn tại, vui lòng kiểm tra đường dẫn có chính xác không");
	}

	/**
	 * Xử lý ngoại lệ tài nguyên tĩnh không tồn tại (Spring Boot 3.2+）
	 * Source Map, khi đường dẫn WebSocket bị xem như yêu cầu tài nguyên tĩnh sẽ kích hoạt ngoại lệ này, hạ xuống log debug để tránh spam
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public Result<?> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
		String uri = request.getRequestURI();
		// Source Map Chỉ dùng để debug trình duyệt, thiếu cũng không ảnh hưởng chức năng nghiệp vụ, hạ xuống log debug
		if (uri.endsWith(".map")) {
			log.debug("Source Map Tài nguyên không tồn tại: {}", uri);
		} else if (uri.contains("Socket/") || uri.contains("websocket/") || uri.contains("Websocket/")) {
			// WebSocket Yêu cầu không phải upgrade của đường dẫn, hạ xuống log debug
			log.debug("WebSocket Đường dẫn bị xem như yêu cầu tài nguyên tĩnh: {}", uri);
		} else {
			log.error(e.getMessage(), e);
			addSysLog(e);
		}
		return Result.error(404, "Đường dẫn không tồn tại, vui lòng kiểm tra đường dẫn có chính xác không");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public Result<?> handleDuplicateKeyException(DuplicateKeyException e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("Bản ghi này đã tồn tại trong cơ sở dữ liệu");
	}

	@ExceptionHandler({UnauthorizedException.class, AuthorizationException.class})
	public Result<?> handleAuthorizationException(AuthorizationException e){
		log.error(e.getMessage(), e);
		return Result.noauth("Không có quyền, vui lòng liên hệ quản trị viên để được cấp quyền!");
	}

	@ExceptionHandler(Exception.class)
	public Result<?> handleException(Exception e){
		log.error(e.getMessage(), e);
		// Giải thích logic mã: Xử lý ngoại lệ giới hạn lưu lượng Sentinel tùy chỉnh
		Throwable throwable = e.getCause();
		SentinelErrorInfoEnum errorInfoEnum = SentinelErrorInfoEnum.getErrorByException(throwable);
		if (ObjectUtil.isNotEmpty(errorInfoEnum)) {
			return Result.error(errorInfoEnum.getError());
		}
		addSysLog(e);
		return Result.error("Thao tác thất bại,"+e.getMessage());
	}
	
	/**
	 * @Author 政辉
	 * @param e
	 * @return
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Result<?> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e){
		StringBuffer sb = new StringBuffer();
		sb.append("Không hỗ trợ");
		sb.append(e.getMethod());
		sb.append("Phương thức yêu cầu,");
		sb.append("Hỗ trợ các phương thức sau");
		String [] methods = e.getSupportedMethods();
		if(methods!=null){
			for(String str:methods){
				sb.append(str);
				sb.append("、");
			}
		}
		log.error(sb.toString(), e);
		//return Result.error("Không có quyền hạn, vui lòng liên hệ quản trị viên để cấp quyền");
		addSysLog(e);
		return Result.error(405,sb.toString());
	}
	
	 /** 
	  * spring Kích thước tải lên mặc định 100MB, bắt ngoại lệ khi vượt quá kích thước MaxUploadSizeExceededException 
	  */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    	log.error(e.getMessage(), e);
		addSysLog(e);
        return Result.error("Kích thước tệp vượt quá giới hạn 10MB, Vui lòng nén hoặc giảm chất lượng tệp! ");
    }

	/**
	 * Xử lý ngoại lệ tệp quá lớn.
	 * jdk17 Trong JDK 17, lớp ngoại lệ MultipartException đã được tách thành MultipartException và MaxUploadSizeExceededException
	 * for [QQYUN-11716]Tải lên hình ảnh lớn thất bại nhưng không có thông báo chính xác
	 * @param e
	 * @return
	 * @author chenrui
	 * @date 2025/4/8 16:13
	 */
	@ExceptionHandler(MultipartException.class)
	public Result<?> handleMaxUploadSizeExceededException(MultipartException e) {
		Throwable cause = e.getCause();
		if (cause instanceof IllegalStateException) {
			log.error("Kích thước tệp vượt quá giới hạn: {}", cause.getMessage(), e);
			addSysLog(e);
			return Result.error("Kích thước tệp vượt quá giới hạn, Vui lòng nén hoặc giảm chất lượng tệp!");
		} else {
			return handleException(e);
		}
	}

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    	log.error(e.getMessage(), e);
		addSysLog(e);
    	//【issues/3624】Thông báo lỗi khi thực thi cơ sở dữ liệu handleDataIntegrityViolationException chưa chính xác #3624
        return Result.error("Lỗi thực thi cơ sở dữ liệu,đã vi phạm tính toàn vẹn, ví dụ: vi phạm ràng buộc duy nhất, vi phạm giới hạn không rỗng, nội dung trường vượt quá độ dài, v.v.");
    }

    @ExceptionHandler(PoolException.class)
    public Result<?> handlePoolException(PoolException e) {
    	log.error(e.getMessage(), e);
		addSysLog(e);
        return Result.error("Redis Ngoại lệ kết nối!");
    }


	/**
	 * SQL Rủi ro injection, xử lý ngoại lệ toàn cục
	 *
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(JeecgSqlInjectionException.class)
	public Result<?> handleSQLException(Exception exception) {
		String msg = exception.getMessage().toLowerCase();
		final String extractvalue = "extractvalue";
		final String updatexml = "updatexml";
		boolean hasSensitiveInformation = msg.indexOf(extractvalue) >= 0 || msg.indexOf(updatexml) >= 0;
		if (msg != null && hasSensitiveInformation) {
			log.error("Kiểm chứng thất bại, tồn tại rủi ro SQL injection!{}", msg);
			return Result.error("Kiểm chứng thất bại, tồn tại rủi ro SQL injection!");
		}
		addSysLog(exception);
		return Result.error("Kiểm chứng thất bại, tồn tại rủi ro SQL injection!" + msg);
	}

	/**
	 * Thêm log hệ thống mới cho ngoại lệ
	 * @param e Ngoại lệ
	 * @author chenrui
	 * @date 2024/4/22 17:16
	 */
    private void addSysLog(Throwable e) {
        LogDTO log = new LogDTO();
        log.setLogType(CommonConstant.LOG_TYPE_4);
        log.setLogContent(e.getClass().getName()+":"+e.getMessage());
		log.setRequestParam(ExceptionUtils.getStackTrace(e));
        //Lấy request
        HttpServletRequest request = null;
        try {
            request = SpringContextUtils.getHttpServletRequest();
        } catch (NullPointerException | BeansException ignored) {
        }
        if (null != request) {
			//Tham số của yêu cầu
			if (!isTooBigException(e)) {
				// Khi xảy ra ngoại lệ tải tệp lên quá lớn thì không thể lấy tham số,nếu không sẽ báo lỗi
				Map<String, String[]> parameterMap = request.getParameterMap();
				if(!CollectionUtils.isEmpty(parameterMap)) {
					//update-begin---author:scott ---date:2026-05-09  for：sys_log.method Độ dài trường là 1000, quá dài khiến ngoại lệ Data truncation nuốt mất lỗi gốc-----------
					String methodStr = oConvertUtils.mapToString(request.getParameterMap());
					if (methodStr != null && methodStr.length() > 950) {
						methodStr = methodStr.substring(0, 950) + "...(truncated)";
					}
					log.setMethod(methodStr);
					//update-end---author:scott ---date:2026-05-09  for：sys_log.method Độ dài trường là 1000, quá dài khiến ngoại lệ Data truncation nuốt mất lỗi gốc-----------
				}
			}
            // Địa chỉ yêu cầu
            log.setRequestUrl(request.getRequestURI());
            //Thiết lập địa chỉ IP
            log.setIp(IpUtils.getIpAddr(request));
            //Thiết lập client
			if(BrowserUtils.isDesktop(request)){
				log.setClientType(ClientTerminalTypeEnum.PC.getKey());
			}else{
				log.setClientType(ClientTerminalTypeEnum.APP.getKey());
			}
        }

       
		//Lấy thông tin người dùng đăng nhập
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if(sysUser!=null){
			log.setUserid(sysUser.getUsername());
			log.setUsername(sysUser.getRealname());

		}

        baseCommonService.addLog(log);
    }

	/**
	 * Có phải là ngoại lệ tệp quá lớn không
	 * for [QQYUN-11716]Tải lên hình ảnh lớn thất bại nhưng không có thông báo chính xác
	 * @param e
	 * @return
	 * @author chenrui
	 * @date 2025/4/8 20:21
	 */
	private static boolean isTooBigException(Throwable e) {
		boolean isTooBigException = false;
		if(e instanceof MultipartException){
			Throwable cause = e.getCause();
			if (cause instanceof IllegalStateException){
				isTooBigException = true;
			}
		}
		if(e instanceof MaxUploadSizeExceededException){
			isTooBigException = true;
		}
		return isTooBigException;
	}

}
