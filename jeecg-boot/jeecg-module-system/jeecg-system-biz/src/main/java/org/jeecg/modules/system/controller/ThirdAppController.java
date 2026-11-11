package org.jeecg.modules.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeecg.dingtalk.api.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.constant.enums.SysRoleEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.jeecg.modules.system.service.ISysThirdAppConfigService;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppFeishuServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.vo.thirdapp.JwSysUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kết nối ứng dụng bên thứ ba
 * @author: jeecg-boot
 */
@Slf4j
@RestController("thirdAppController")
@RequestMapping("/sys/thirdApp")
public class ThirdAppController {

    @Autowired
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
    @Autowired
    ThirdAppDingtalkServiceImpl dingtalkService;
    @Autowired
    ThirdAppFeishuServiceImpl feishuService;

    @Autowired
    private ISysThirdAppConfigService appConfigService;

    @Autowired
    private ISysThirdAccountService sysThirdAccountService;

    //update-begin---author:scott ---date:20261020  for: Thêm kiểm tra quyền quản lý cấu hình tích hợp bên thứ ba (dùng cho nhánh quản trị viên tài khoản tổ chức của tenant này)-----------
    @Autowired
    private ISysTenantPackService sysTenantPackService;
    //update-end---author:scott ---date:20261020  for：-----------

    /**
     * Lấy hệ thống đang bật
     */
    @GetMapping("/getEnabledType")
    public Result getEnabledType() {
        Map<String, Boolean> enabledMap = new HashMap(5);
        int tenantId;
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            tenantId = oConvertUtils.getInt(TenantContext.getTenant(), -1);
        } else {
            tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        }
        //Truy vấn cấu hình bên thứ ba thuộc tenant hiện tại
        List<SysThirdAppConfig> list = appConfigService.getThirdConfigListByThirdType(tenantId);
        //DingTalk đã được cấu hình chưa
        boolean dingConfig = false;
        //WeChat doanh nghiệp đã được cấu hình hay chưa
        boolean qywxConfig = false;
        //Feishu đã được cấu hình chưa
        boolean feishuConfig = false;
        if(null != list && list.size()>0){
            for (SysThirdAppConfig config:list) {
                if(MessageTypeEnum.DD.getType().equals(config.getThirdType())){
                    dingConfig = true;
                    continue;
                }
                if(MessageTypeEnum.QYWX.getType().equals(config.getThirdType())){
                    qywxConfig = true;
                    continue;
                }
                if(MessageTypeEnum.FS.getType().equals(config.getThirdType())){
                    feishuConfig = true;
                    continue;
                }
            }
        }
        enabledMap.put("wechatEnterprise", qywxConfig);
        enabledMap.put("dingtalk", dingConfig);
        enabledMap.put("feishu", feishuConfig);
        return Result.OK(enabledMap);
    }

    /**
     * Đồng bộ cục bộ[Người dùng]đến 
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/user/toApp")
    public Result syncWechatEnterpriseUserToApp(@RequestParam(value = "ids", required = false) String ids) {
        //Lấy cấu hình WeChat doanh nghiệp
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        if (null != config) {
        // Giải thích logic mã: [QQYUN-3440]Cách ly theo chế độ tenant ------------
            SyncInfoVo syncInfo = wechatEnterpriseService.syncLocalUserToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Đồng bộ thành công", syncInfo);
            } else {
                return Result.error("Đồng bộ thất bại", syncInfo);
            }
        }
        return Result.error("WeChat doanh nghiệp chưa được cấu hình,Vui lòng cấu hình WeChat Doanh nghiệp");
    }

    /**
     * Đồng bộ 【WeChat doanh nghiệp】[Người dùng]về máy local
     *
     * @param ids Hủy bỏ
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/user/toLocal")
    public Result syncWechatEnterpriseUserToLocal(@RequestParam(value = "ids", required = false) String ids) {
        return Result.error("Do điều chỉnh giao diện WeChat doanh nghiệp, chức năng đồng bộ về local đã không còn hiệu lực");

//        if (thirdAppConfig.isWechatEnterpriseEnabled()) {
//            SyncInfoVo syncInfo = wechatEnterpriseService.syncThirdAppUserToLocal();
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Đồng bộ thành công", syncInfo);
//            } else {
//                return Result.error("Đồng bộ thất bại", syncInfo);
//            }
//        }
//        return Result.error("Chức năng đồng bộ WeChat Work đã bị vô hiệu hóa");
    }

    /**
     * Đồng bộ cục bộ[Phòng ban]đến 
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/depart/toApp")
    public Result syncWechatEnterpriseDepartToApp(@RequestParam(value = "ids", required = false) String ids) {
        //Lấy cấu hình WeChat doanh nghiệp
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        if (null != config) {
            SyncInfoVo syncInfo = wechatEnterpriseService.syncLocalDepartmentToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Đồng bộ thành công", null);
            } else {
                return Result.error("Đồng bộ thất bại", syncInfo);
            }
        }
        return Result.error("WeChat doanh nghiệp chưa được cấu hình,Vui lòng cấu hình WeChat Doanh nghiệp");
    }

    /**
     * Đồng bộ 【WeChat doanh nghiệp】[Phòng ban]về máy local
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/depart/toLocal")
    public Result syncWechatEnterpriseDepartToLocal(@RequestParam(value = "ids", required = false) String ids) {
        return Result.error("Do điều chỉnh giao diện WeChat doanh nghiệp, việc đồng bộ phòng ban local của WeChat doanh nghiệp không còn hiệu lực");
//        //Lấy cấu hình WeChat doanh nghiệp
//        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
//        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
//        if (null != config) {
//            SyncInfoVo syncInfo = wechatEnterpriseService.syncThirdAppDepartmentToLocal(ids);
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Đồng bộ thành công", syncInfo);
//            } else {
//                return Result.error("Đồng bộ thất bại", syncInfo);
//            }
//        }
//        return Result.error("WeChat doanh nghiệp chưa được cấu hình,Vui lòng cấu hình WeChat Doanh nghiệp");
    }

    /**
     * Đồng bộ cục bộ[Phòng ban]đến【DingTalk】
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/dingtalk/depart/toApp")
    public Result syncDingtalkDepartToApp(@RequestParam(value = "ids", required = false) String ids) {
        //Lấy cấu hình DingTalk
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        if (null != config) {
            SyncInfoVo syncInfo = dingtalkService.syncLocalDepartmentToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Đồng bộ thành công", null);
            } else {
                return Result.error("Đồng bộ thất bại", syncInfo);
            }
        }
        return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
    }

//    /**
//     * Đồng bộ 【DingTalk】[Phòng ban]về máy local
//     *
//     * @param ids
//     * @return
//     */
//   @GetMapping("/sync/dingtalk/depart/toLocal")
//    public Result syncDingtalkDepartToLocal(@RequestParam(value = "ids", required = false) String ids) {
//        //Lấy cấu hình DingTalk
//        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
//        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
//        if (null!= config) {
//            SyncInfoVo syncInfo = dingtalkService.syncThirdAppDepartmentToLocal(ids);
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Đồng bộ thành công", syncInfo);
//            } else {
//                return Result.error("Đồng bộ thất bại", syncInfo);
//            }
//        }
//        return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
//    }

    /**
     * Đồng bộ cục bộ[Người dùng]đến【DingTalk】
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/dingtalk/user/toApp")
    public Result syncDingtalkUserToApp(@RequestParam(value = "ids", required = false) String ids) {
        //Lấy cấu hình DingTalk
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        //Theo id tenant và loại bên thứ ba, lấy dữ liệu tenant
        SysThirdAppConfig appConfig = appConfigService.getThirdConfigByThirdType(tenantId,MessageTypeEnum.DD.getType());
        if(null != appConfig){
            SyncInfoVo syncInfo = dingtalkService.syncLocalUserToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Đồng bộ thành công", syncInfo);
            } else {
                return Result.error("Đồng bộ thất bại", syncInfo);
            }
        }
        return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
    }

//    /**
//     * Đồng bộ 【DingTalk】[Người dùng]về máy local
//     *
//     * @param ids Hủy bỏ
//     * @return
//     */
//    @GetMapping("/sync/dingtalk/user/toLocal")
//    public Result syncDingtalkUserToLocal(@RequestParam(value = "ids", required = false) String ids) {
//        //Lấy cấu hình DingTalk
//        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
//        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
//        if (null != config) {
//            SyncInfoVo syncInfo = dingtalkService.syncThirdAppUserToLocal();
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Đồng bộ thành công", syncInfo);
//            } else {
//                return Result.error("Đồng bộ thất bại", syncInfo);
//            }
//        }
//        return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
//    }

    /**
     * Kiểm thử gửi tin nhắn
     *
     * @return
     */
    @PostMapping("/sendMessageTest")
    public Result sendMessageTest(@RequestBody JSONObject params, HttpServletRequest request) {
        /* Lấy tham số truyền từ frontend */
        // Loại app bên thứ ba
        String app = params.getString("app");
        // Có gửi cho tất cả mọi người không
        boolean sendAll = params.getBooleanValue("sendAll");
        // Người nhận tin nhắn, truyền sys_user Trường username của bảng, nhiều giá trị phân tách bằng dấu phẩy
        String receiver = params.getString("receiver");
        // Nội dung tin nhắn
        String content = params.getString("content");
        // Tenant id
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);

        String fromUser = JwtUtil.getUserNameByToken(request);
        String title = "Kiểm tra tin nhắn APP bên thứ ba";
        MessageDTO message = new MessageDTO(fromUser, receiver, title, content);
        message.setToAll(sendAll);
        // Giải thích logic mã: [QQYUN-3440]DingTalk, WeChat doanh nghiệp cách ly theo chế độ tenant ------------
        String weChatType = MessageTypeEnum.QYWX.getType();
        String dingType = MessageTypeEnum.DD.getType();
        if (weChatType.toUpperCase().equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, weChatType);
            if (null != config) {
                JSONObject response = wechatEnterpriseService.sendMessageResponse(message, false);
                return Result.OK(response);
            }
            return Result.error("WeChat doanh nghiệp chưa được cấu hình,Vui lòng cấu hình WeChat Doanh nghiệp");
        } else if (dingType.toUpperCase().equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, dingType);
            if (null != config) {
                Response<String> response = dingtalkService.sendMessageResponse(message, false);
                return Result.OK(response);
            }
            return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
        }
        String feishuType = MessageTypeEnum.FS.getType();
        if (feishuType.toUpperCase().equals(app) || CommonConstant.FEISHU.equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, feishuType);
            if (null != config) {
                boolean success = feishuService.sendMessage(message, false);
                return success ? Result.OK("Gửi tin nhắn Feishu thành công") : Result.error("Gửi tin nhắn Feishu thất bại, vui lòng kiểm tra người nhận đã liên kết tài khoản Feishu hay chưa");
            }
            return Result.error("Feishu chưa được cấu hình, vui lòng cấu hình ứng dụng Feishu");
        }
        return Result.error("Không nhận dạng được ứng dụng bên thứ ba APP");
    }

    /**
     * Kiểm thử thu hồi tin nhắn
     *
     * @return
     */
    @PostMapping("/recallMessageTest")
    public Result recallMessageTest(@RequestBody JSONObject params) {
        /* Lấy tham số truyền từ frontend */
        // Loại app bên thứ ba
        String app = params.getString("app");
        // Tin nhắn id
        String msgTaskId = params.getString("msg_task_id");
        //Tenant id
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        if (CommonConstant.WECHAT_ENTERPRISE.equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
            if (null != config) {
                return Result.error("WeChat Work không hỗ trợ thu hồi tin nhắn");
            }
            return Result.error("WeChat doanh nghiệp chưa được cấu hình,Vui lòng cấu hình WeChat Doanh nghiệp");
        } else if (CommonConstant.DINGTALK.equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
            if (null != config) {
                Response<JSONObject> response = dingtalkService.recallMessageResponse(msgTaskId);
                if (response.isSuccess()) {
                    return Result.OK("Thu hồi thành công", response);
                } else {
                    return Result.error("Thu hồi thất bại:" + response.getErrcode() + "——" + response.getErrmsg(), response);
                }
            }
            return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
        }
        return Result.error("Không nhận dạng được ứng dụng bên thứ ba APP");
    }

    //========================begin Ứng dụng low-code DingTalk/Dùng riêng để đồng bộ phòng ban người dùng WeChat doanh nghiệp =============================
    /**
     * Thêm cấu hình app bên thứ ba
     *
     * @param appConfig
     * @return
     */
    @RequestMapping(value = "/addThirdAppConfig", method = RequestMethod.POST)
    public Result<String> addThirdAppConfig(@RequestBody SysThirdAppConfig appConfig) {
        Result<String> result = new Result<>();
        //Dựa theo id thuê bao đang đăng nhập và loại bên thứ ba để xác định đã tạo hay chưa
        Integer tenantId = oConvertUtils.isNotEmpty(appConfig.getTenantId()) ? appConfig.getTenantId() : oConvertUtils.getInt(TenantContext.getTenant(), 0);
        //update-begin---author:scott ---date:20261020  for: quyền hạn+Kiểm tra kép cấp đối tượng:admin/system:third:config:add Mã quyền hạn/Một trong ba quản trị viên tài khoản tổ chức của tenant này được cho qua, ngăn người dùng đăng nhập tùy ý tạo cấu hình bên thứ ba của tenant khác-----------
        assertThirdConfigManager(tenantId, "system:third:config:add");
        //update-end---author:scott ---date:20261020  for：-----------
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, appConfig.getThirdType());
        if (null != config) {
            result.error500("Thao tác thất bại,Trong cùng một tenant chỉ cho phép liên kết một DingTalk hoặc một WeChat doanh nghiệp");
            return result;
        }
        String clientId = appConfig.getClientId();
        //Lấy cấu hình bên thứ ba qua key ứng dụng
        List<SysThirdAppConfig> thirdAppConfigByClientId = appConfigService.getThirdAppConfigByClientId(clientId);
        if(CollectionUtil.isNotEmpty(thirdAppConfigByClientId)){
            result.error500("AppKey Đã tồn tại, vui lòng không thêm lặp lại");
            return result;
        }
        try {
            appConfig.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
            appConfigService.save(appConfig);
            result.success("Thêm thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }

    /**
     * Chỉnh sửa cấu hình app bên thứ ba
     *
     * @param appConfig
     * @return
     */
    @RequestMapping(value = "/editThirdAppConfig", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> editThirdAppConfig(@RequestBody SysThirdAppConfig appConfig) {
        Result<String> result = new Result<>();
        SysThirdAppConfig config = appConfigService.getById(appConfig.getId());
        if (null == config) {
            result.error500("Dữ liệu không tồn tại");
            return result;
        }
        //update-begin---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
        assertThirdConfigManager(config.getTenantId(), "system:third:config:edit");
        //update-end---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
        String clientId = appConfig.getClientId();
        //Nếu ứng dụng đang chỉnh sửa key,không khớp với trong cơ sở dữ liệu thì cần kiểm tra app key đã tồn tại hay chưa
        if(!clientId.equals(config.getClientId())){
            //Lấy cấu hình bên thứ ba qua key ứng dụng
            List<SysThirdAppConfig> thirdAppConfigByClientId = appConfigService.getThirdAppConfigByClientId(clientId);
            if(CollectionUtil.isNotEmpty(thirdAppConfigByClientId)){
                result.error500("AppKey Đã tồn tại, vui lòng không thêm lặp lại");
                return result;
            }
        }
        //update-begin---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
        config.setAgentId(appConfig.getAgentId());
        config.setClientId(clientId);
        if (oConvertUtils.isNotEmpty(appConfig.getClientSecret())) {
            config.setClientSecret(appConfig.getClientSecret());
        }
        config.setCorpId(appConfig.getCorpId());
        config.setStatus(appConfig.getStatus());
        //update-end---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
        try {
            appConfigService.updateById(config);
            result.success("Chỉnh sửa thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }

    /**
     * Xóa bảng cấu hình bên thứ ba theo id
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deleteThirdAppConfig")
    @RequiresPermissions("system:third:config:delete")
    public Result<String> deleteThirdAppConfig(@RequestParam(name="id",required=true) String id) {
        Result<String> result = new Result<>();
        SysThirdAppConfig config = appConfigService.getById(id);
        if (null == config) {
            result.error500("Dữ liệu không tồn tại");
            return result;
        }
        try {
            appConfigService.removeById(id);
            result.success("Hủy liên kết thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }
    

    /**
     * Lấy thông tin cấu hình app bên thứ ba theo id thuê bao và loại bên thứ ba
     *
     * @param tenantId
     * @param thirdType
     * @return
     */
    @GetMapping("/getThirdConfigByTenantId")
    public Result<SysThirdAppConfig> getThirdAppByTenantId(@RequestParam(name = "tenantId", required = false) Integer tenantId,
                                                           @RequestParam(name = "thirdType") String thirdType) {
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            if (tenantId == null) {
                return Result.error("Đã bật chế độ đa tenant, tham số ID tenant không được phép rỗng!");
            }
        } else {
            //Nếu Tenant không được truyền vào, sẽ dùng của nền tảng
            if (tenantId == null) {
                tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
            }
        }
        //update-begin---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
        assertThirdConfigManager(tenantId, "system:third:config:query");
        //update-end---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
        Result<SysThirdAppConfig> result = new Result<>();
        LambdaQueryWrapper<SysThirdAppConfig> query = new LambdaQueryWrapper<>();
        query.eq(SysThirdAppConfig::getThirdType,thirdType);
        query.eq(SysThirdAppConfig::getTenantId,tenantId);
        SysThirdAppConfig sysThirdAppConfig = appConfigService.getOne(query);
        result.setSuccess(true);
        result.setResult(sysThirdAppConfig);
        return result;
    }

    /**
     * Đồng bộ 【DingTalk】[Phòng ban và người dùng]về máy local
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/dingtalk/departAndUser/toLocal")
    public Result syncDingTalkDepartAndUserToLocal(@RequestParam(value = "ids", required = false) String ids) {
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        if (null != config) {
            SyncInfoVo syncInfo = dingtalkService.syncThirdAppDepartmentUserToLocal();
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Đồng bộ thành công", syncInfo);
            } else {
                return Result.error("Đồng bộ thất bại", syncInfo);
            }
        }
        return Result.error("DingTalk chưa được cấu hình,Vui lòng cấu hình DingTalk");
    }
    //update-begin---author:liusq ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-Đồng bộ phòng ban và người dùng Feishu về cục bộ-----------
    /**
     * Đồng bộ danh bạ 【Feishu】 (phòng ban + người dùng) về máy cục bộ
     * Ứng dụng Feishu cần mở quyền hạn：contact:department:read、contact:user.base:readonly
     */
    @GetMapping("/sync/feishu/departAndUser/toLocal")
    public Result<SyncInfoVo> syncFeishuDepartAndUserToLocal(HttpServletRequest request) {
        int tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.FS.getType());
        if (null != config) {
            SyncInfoVo syncInfo = feishuService.syncFeishuDeptAndUserToLocal(tenantId);
            if (syncInfo.getFailInfo().isEmpty()) {
                return Result.OK("Đồng bộ thành công", syncInfo);
            } else {
                return Result.error("Đồng bộ hoàn tất (bao gồm mục thất bại, vui lòng xem chi tiết)", syncInfo);
            }
        }
        return Result.error("Feishu chưa được cấu hình, vui lòng cấu hình ứng dụng Feishu");
    }
    //update-end---author:liusq ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-Đồng bộ phòng ban và người dùng Feishu về cục bộ-----------

    //========================end Ứng dụng low-code DingTalk/Dùng riêng để đồng bộ phòng ban người dùng WeChat doanh nghiệp ========================


    //========================begin Ứng dụng low-code: thiết lập liên kết tài khoản bên thứ ba cho tài khoản ================================
    /**
     * Lấy tài khoản bên thứ ba
     * @param thirdType
     * @return
     */
    @GetMapping("/getThirdAccountByUserId")
    public Result<List<SysThirdAccount>> getThirdAccountByUserId(@RequestParam(name="thirdType") String thirdType){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
        //Truy vấn theo id
        query.eq(SysThirdAccount::getSysUserId,sysUser.getId());
        //Đăng nhập bằng mã QR chỉ khi tenant là 0
        query.eq(SysThirdAccount::getTenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        //Truy vấn theo loại bên thứ ba
        if(oConvertUtils.isNotEmpty(thirdType)){
            query.in(SysThirdAccount::getThirdType, Arrays.asList(thirdType.split(SymbolConstant.COMMA)));
        }
        List<SysThirdAccount> list = sysThirdAccountService.list(query);
        return Result.ok(list);
    }

    /**
     * Liên kết tài khoản bên thứ ba
     * @return
     */
    @PostMapping("/bindThirdAppAccount")
    public Result<SysThirdAccount> bindThirdAppAccount(@RequestBody SysThirdAccount sysThirdAccount){
        SysThirdAccount thirdAccount = sysThirdAccountService.bindThirdAppAccountByUserId(sysThirdAccount);
        return Result.ok(thirdAccount);
    }

    /**
     * Xóa thông tin người dùng bên thứ ba
     * @param sysThirdAccount
     * @return
     */
    @DeleteMapping("/deleteThirdAccount")
    public Result<String> deleteThirdAccountById(@RequestBody SysThirdAccount sysThirdAccount){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(!sysUser.getId().equals(sysThirdAccount.getSysUserId())){
            return Result.error("Không có quyền sửa thông tin của người khác");
        }
        SysThirdAccount thirdAccount = sysThirdAccountService.getById(sysThirdAccount.getId());
        if(null == thirdAccount){
            return Result.error("Không tìm thấy thông tin tài khoản bên thứ ba đó");
        }
        sysThirdAccountService.removeById(thirdAccount.getId());
        return Result.ok("Hủy liên kết thành công");
    }
    //========================end Ứng dụng low-code: thiết lập liên kết tài khoản bên thứ ba cho tài khoản ================================

    /**
     * Lấy thông tin người dùng đã liên kết WeChat doanh nghiệp
     * @param request
     * @return
     */
    @GetMapping("/getThirdUserByWechat")
    public Result<JwSysUserDepartVo> getThirdUserByWechat(HttpServletRequest request){
        //Lấy cấu hình WeChat doanh nghiệp
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        if (null != config) {
            JwSysUserDepartVo list = wechatEnterpriseService.getThirdUserByWechat(tenantId);
            return Result.ok(list);
        }
        return Result.error("WeChat doanh nghiệp chưa được cấu hình,Vui lòng cấu hình WeChat Doanh nghiệp"); 
    }

    /**
     * Đồng bộ phòng ban và người dùng WeChat doanh nghiệp về cục bộ
     * @param jwUserDepartJson
     * @param request
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/departAndUser/toLocal")
    public Result<SyncInfoVo> syncWechatEnterpriseDepartAndUserToLocal(@RequestParam(name = "jwUserDepartJson") String jwUserDepartJson,HttpServletRequest request){
        int tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
        SyncInfoVo syncInfoVo = wechatEnterpriseService.syncWechatEnterpriseDepartAndUserToLocal(jwUserDepartJson,tenantId);
        return Result.ok(syncInfoVo);
    }

    /**
     * Truy vấn người dùng WeChat Work đã được liên kết
     * @param request
     * @return
     */
    @GetMapping("/getThirdUserBindByWechat")
    public Result<List<JwUserDepartVo>> getThirdUserBindByWechat(HttpServletRequest request){
        int tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
        List<JwUserDepartVo> jwSysUserDepartVos = wechatEnterpriseService.getThirdUserBindByWechat(tenantId);
        return Result.ok(jwSysUserDepartVos);
    }

    //update-begin---author:wangshuai ---date:2026101  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không ---
    /*
     * Kiểm tra người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của thuê bao được chỉ định hay không:admin/Quản trị viên tenant hệ thống được cho qua; người giữ bất kỳ mã quyền nào trong permCodes được cho qua,
     * Nếu không thì phải là quản trị viên tài khoản tổ chức hoặc siêu quản trị viên của tenant đó (dùng cho tự cấu hình tại trang tổ chức QiaoQiaoYun, tương ứng sys_tenant_pack_user）。
     *
     * @param tenantId  Tenant ID
     * @param permCodes Mã quyền hạn nút được phép cho qua (thêm mới/Chỉnh sửa/Truy vấn các mã quyền hạn độc lập của từng bên, phía gọi truyền mã tương ứng theo hành động của mình)
     * @author wangshuai
     */
    private void assertThirdConfigManager(Integer tenantId, String... permCodes) {
        if (SysRoleEnum.hasAnyRole(SysRoleEnum.ADMIN, SysRoleEnum.TENANT_ADMIN)) {
            return;
        }
        for (String permCode : permCodes) {
            if (SecurityUtils.getSubject().isPermitted(permCode)) {
                return;
            }
        }
        try {
            sysTenantPackService.izHaveManageUserAuth(String.valueOf(tenantId));
        } catch (Exception e) {
            throw new JeecgBootBizTipException("Không có quyền hạn, vui lòng liên hệ quản trị viên");
        }
    }
    //update-end---author:wangshuai ---date:20261021  for: Kiểm tra xem người dùng hiện tại có thể quản lý cấu hình tích hợp bên thứ ba của tenant được chỉ định hay không-----------
}
