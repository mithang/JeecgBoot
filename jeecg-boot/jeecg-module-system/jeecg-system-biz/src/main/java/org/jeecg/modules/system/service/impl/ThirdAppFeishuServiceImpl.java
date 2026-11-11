package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import me.zhyd.oauth.config.AuthConfig;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysThirdAppConfigMapper;
import org.jeecg.modules.system.mapper.SysTenantMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserTenantMapper;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

//update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-----------
/**
 * Kết nối App bên thứ ba: lớp triển khai Feishu
 *
 * @author jeecg-boot
 * @date 2026-05-13
 */
@Slf4j
@Service
public class ThirdAppFeishuServiceImpl implements IThirdAppService {

    // ==================== Hằng số OpenAPI của Feishu ====================

    private static final String FEISHU_BASE = "https://open.feishu.cn";
    /** Lấy ứng dụng app_access_token(dùng để đổi lấy token người dùng khi đăng nhập OAuth2) */
    private static final String APP_ACCESS_TOKEN_URL = FEISHU_BASE + "/open-apis/auth/v3/app_access_token/internal";
    /** Lấy ứng dụng tenant_access_token(dùng cho các giao diện cấp doanh nghiệp như đồng bộ danh bạ, gửi tin nhắn) */
    private static final String APP_TOKEN_URL     = FEISHU_BASE + "/open-apis/auth/v3/tenant_access_token/internal";
    /** OAuth2 code đổi lấy user_access_token(giao diện OIDC phiên bản mới, Authorization: Bearer app_access_token） */
    private static final String USER_TOKEN_URL    = FEISHU_BASE + "/open-apis/authen/v1/oidc/access_token";
    /** Lấy thông tin người dùng đã đăng nhập */
    private static final String USER_INFO_URL     = FEISHU_BASE + "/open-apis/authen/v1/user_info";
    /** OAuth2 Địa chỉ ủy quyền (phiên bản mới, thay thế địa chỉ đã bị loại bỏ /authen/v1/index） */
    private static final String AUTHORIZE_URL     = FEISHU_BASE + "/open-apis/authen/v1/authorize";
    /** Gửi tin nhắn (gửi đơn,receive_id_type=open_id） */
    private static final String SEND_MSG_URL      = FEISHU_BASE + "/open-apis/im/v1/messages";
    /** Lấy danh sách phòng ban con (phân trang) */
    private static final String DEPT_CHILDREN_URL = FEISHU_BASE + "/open-apis/contact/v3/departments/%s/children";
    /** Lấy chi tiết một phòng ban (dùng để lấy phòng ban gốc open_department_id） */
    private static final String DEPT_DETAIL_URL   = FEISHU_BASE + "/open-apis/contact/v3/departments/%s";
    /** Lấy thông tin tenant (bao gồm tên công ty, phòng ban gốc ảo"0"bản thân không có tên, cần lấy tên công ty từ giao diện này) */
    private static final String TENANT_INFO_URL   = FEISHU_BASE + "/open-apis/tenant/v2/tenant/query";
    /** Lấy người dùng trực thuộc phòng ban (phân trang) */
    private static final String DEPT_USERS_URL    = FEISHU_BASE + "/open-apis/contact/v3/users";
    //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, đổi sang dùng trường feishuIdentifier để lưu ID phòng ban, bỏ phương án tiền tố memo-----------
    //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, đổi sang dùng trường feishuIdentifier để lưu ID phòng ban, bỏ phương án tiền tố memo-----------

    /** Giá trị cố định của loại bên thứ ba, tương ứng với sys_third_app_config.third_type */
    public static final String THIRD_TYPE = MessageTypeEnum.FS.getType();

    // ==================== Tiêm phụ thuộc ====================

    @Autowired
    private SysThirdAppConfigMapper configMapper;
    @Autowired
    private SysTenantMapper tenantMapper;
    @Autowired
    private ISysThirdAccountService sysThirdAccountService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysUserDepartService sysUserDepartService;
    //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, inject bản ghi gửi thông báo mapper-----------
    @Autowired
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;
    //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, inject bản ghi gửi thông báo mapper-----------
    //update-begin---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Tích hợp Feishu, inject cấu hình justauth để dùng cho yml fallback-----------
    @Autowired(required = false)
    private JustAuthProperties justAuthProperties;
    //update-end---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Tích hợp Feishu, inject cấu hình justauth để dùng cho yml fallback-----------
    @Autowired
    private SysUserTenantMapper userTenantMapper;

    // ==================== IThirdAppService Triển khai giao diện ====================

    @Override
    public String getAccessToken() {
        //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767】Tích hợp Feishu, đổi sang dùng phương pháp lấy cấu hình thống nhất-----------
        SysThirdAppConfig config = getFeishuThirdAppConfig();
        //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767】Tích hợp Feishu, đổi sang dùng phương pháp lấy cấu hình thống nhất-----------
        if (config != null) {
            return getTenantAccessToken(config);
        }
        log.warn("Tenant hiện tại chưa cấu hình ứng dụng Feishu");
        return null;
    }

    /** Cục bộ→Đồng bộ phòng ban Feishu (Feishu tạm thời không hỗ trợ ghi ngược, giữ lại giao diện) */
    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        syncInfo.addFailInfo("Feishu tạm thời không hỗ trợ hướng cục bộ→Đồng bộ phòng ban theo hướng Feishu, vui lòng quản lý danh bạ trong trang quản trị Feishu rồi mới thực hiện đồng bộ Feishu→Đồng bộ cục bộ");
        return syncInfo;
    }

    /** Cục bộ→Đồng bộ người dùng Feishu (Feishu tạm thời không hỗ trợ ghi ngược, giữ lại giao diện) */
    @Override
    public SyncInfoVo syncLocalUserToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        syncInfo.addFailInfo("Feishu tạm thời không hỗ trợ hướng cục bộ→Đồng bộ người dùng theo hướng Feishu, vui lòng quản lý danh bạ trong trang quản trị Feishu rồi mới thực hiện đồng bộ Feishu→Đồng bộ cục bộ");
        return syncInfo;
    }

    @Override
    public int removeThirdAppUser(List<String> userIdList) {
        return 0;
    }

    @Override
    public boolean sendMessage(MessageDTO message) {
        return sendMessage(message, false);
    }

    @Override
    public boolean sendMessage(MessageDTO message, boolean verifyConfig) {

        try {
            int tenantId = MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL
                    ? oConvertUtils.getInt(TenantContext.getTenant(), 0) : 0;
            SysThirdAppConfig config = getFeishuThirdAppConfig();
            if (config == null) {
                if (verifyConfig) {
                    log.warn("Ứng dụng Feishu chưa được cấu hình, bỏ qua gửi tin nhắn");
                }
                return false;
            }
            String appToken = getTenantAccessToken(config);
            if (appToken == null) {
                log.error("Feishu lấy tenant_access_token thất bại, việc gửi tin nhắn bị dừng");
                return false;
            }
            String toUser = message.getToUser();
            if (StringUtils.isEmpty(toUser)) {
                log.warn("Người nhận tin nhắn Feishu trống, bỏ qua gửi");
                return false;
            }
            boolean allSuccess = true;
            for (String username : toUser.split(",")) {
                if (StringUtils.isEmpty(username.trim())) {
                    continue;
                }
                SysUser sysUser = sysUserService.getUserByName(username.trim());
                if (sysUser == null) {
                    log.warn("Gửi tin nhắn Feishu: người dùng không tồn tại,username={}", username);
                    allSuccess = false;
                    continue;
                }
                LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
                query.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
                query.eq(SysThirdAccount::getTenantId, tenantId);
                query.eq(SysThirdAccount::getSysUserId, sysUser.getId());
                //update-begin---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Khi tồn tại tài khoản bên thứ ba Feishu trùng lặp thì không ném TooManyResultsException nữa, lấy bản ghi đầu tiên để gửi-----------
                SysThirdAccount thirdAccount = sysThirdAccountService.getOne(query, false);
                //update-end---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Khi tồn tại tài khoản bên thứ ba Feishu trùng lặp thì không ném TooManyResultsException nữa, lấy bản ghi đầu tiên để gửi-----------
                if (thirdAccount == null || StringUtils.isEmpty(thirdAccount.getThirdUserUuid())) {
                    log.warn("Feishu gửi tin nhắn: người dùng[{}]Chưa liên kết tài khoản Feishu", username);
                    allSuccess = false;
                    continue;
                }
                //update-begin---author:scott ---date:2026-10-09  for: [QiaoQiaoYun] JianLiu — thông báo tích hợp Feishu, nội dung Markdown đổi sang gửi tin nhắn dạng thẻ, tránh hiển thị văn bản Markdown gốc trong Feishu-----------
                boolean sent = message.isMarkdown()
                        ? doSendMarkdownCardMessage(appToken, thirdAccount.getThirdUserUuid(), message.getTitle(), message.getContent())
                        : doSendTextMessage(appToken, thirdAccount.getThirdUserUuid(), message.getContent());
                if (!sent) {
                    allSuccess = false;
                }
                //update-end---author:scott ---date:2026-10-09  for: [QiaoQiaoYun] JianLiu — thông báo tích hợp Feishu, nội dung Markdown đổi sang gửi tin nhắn dạng thẻ, tránh hiển thị văn bản Markdown gốc trong Feishu-----------
            }
            return allSuccess;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Đăng nhập Feishu OAuth2 ====================

    /**
     * Xây dựng địa chỉ ủy quyền đăng nhập quét mã OAuth2 Feishu (phiên bản mới /authen/v1/authorize）
     * <p>Thay thế JustAuth 1.16.1 đã bị loại bỏ mà JustAuth 1.16.1 từng dùng /authen/v1/index endpoint</p>
     *
     * @param config      Cấu hình ứng dụng Feishu trong cơ sở dữ liệu
     * @param redirectUri Địa chỉ callback (phải nhất quán với cài đặt bảo mật ở hậu trường nhà phát triển Feishu)
     * @param state       Giá trị trạng thái ngẫu nhiên, dùng để chống CSRF và truyền xuyên suốt thông tin tenant
     * @return Trang ủy quyền Feishu URL
     */
    public String buildAuthorizeUrl(SysThirdAppConfig config, String redirectUri, String state) {
        try {
            return AUTHORIZE_URL
                    + "?app_id=" + URLEncoder.encode(config.getClientId(), "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                    + "&state=" + URLEncoder.encode(state, "UTF-8");
        } catch (Exception e) {
            log.error("Tạo địa chỉ ủy quyền Feishu thất bại", e);
            return null;
        }
    }

    /**
     * Luồng chính đăng nhập OAuth2 của Feishu:code → user_access_token → Thông tin người dùng → Liên kết người dùng hệ thống
     * <p>Gọi trực tiếp API phiên bản mới của Feishu, bỏ qua JustAuth 1.16.1 Giao diện đã bị loại bỏ trong</p>
     *
     * @param code        Mã ủy quyền Feishu callback trả về
     * @param tenantId    Tenant hiện tại ID
     * @param redirectUri Địa chỉ callback (phải nhất quán với yêu cầu ủy quyền)
     * @return Người dùng hệ thống đã liên kết
     */
    public SysUser oauth2Login(String code, Integer tenantId, String redirectUri) {
        //update-begin---author:liusq ---date:2026-05-15  for：【QQYUN-12767] Đăng nhập quét mã Feishu đổi sang dùng cấu hình yml, ủy thác cho phương thức nạp chồng có tham số cấu hình (null=đọc cơ sở dữ liệu)-----------
        return oauth2Login(code, tenantId, redirectUri, null);
        //update-end---author:liusq ---date:2026-05-15  for：【QQYUN-12767] Đăng nhập quét mã Feishu đổi sang dùng cấu hình yml, ủy thác cho phương thức nạp chồng có tham số cấu hình (null=đọc cơ sở dữ liệu)-----------
    }

    //update-begin---author:liusq ---date:2026-05-15  for：【QQYUN-12767] Đăng nhập quét mã Feishu đổi sang dùng cấu hình yml, thêm phương thức nạp chồng hỗ trợ truyền cấu hình từ bên ngoài vào-----------
    /**
     * Luồng chính đăng nhập OAuth2 Feishu: hỗ trợ truyền cấu hình ứng dụng từ bên ngoài vào
     * <p>Đăng nhập quét mã truyền vào cấu hình đọc từ yml; đăng nhập nhúng OAuth2 truyền null, tự động lấy từ sys_third_app_config Truy vấn</p>
     *
     * @param code           Mã ủy quyền Feishu callback trả về
     * @param tenantId       Tenant hiện tại ID
     * @param redirectUri    Địa chỉ callback (phải nhất quán với yêu cầu ủy quyền)
     * @param externalConfig Cấu hình truyền vào từ bên ngoài (khi null thì truy vấn từ cơ sở dữ liệu)
     * @return Người dùng hệ thống đã liên kết
     */
    public SysUser oauth2Login(String code, Integer tenantId, String redirectUri, SysThirdAppConfig externalConfig) {
        SysThirdAppConfig config = (externalConfig != null) ? externalConfig : configMapper.getThirdConfigByThirdType(tenantId, THIRD_TYPE);
        if (config == null) {
            throw new JeecgBootException("Ứng dụng Feishu chưa được cấu hình, vui lòng liên hệ quản trị viên");
        }
        // Step1: code đổi lấy user_access_token
        String userAccessToken = getUserAccessToken(config, code, redirectUri);
        if (userAccessToken == null) {
            throw new JeecgBootException("Feishu lấy user_access_token Thất bại, vui lòng kiểm tra cấu hình ứng dụng và địa chỉ callback");
        }
        // Step2: Lấy thông tin người dùng
        JSONObject userInfo = getFeishuUserInfo(userAccessToken);
        if (userInfo == null) {
            throw new JeecgBootException("Lấy thông tin người dùng Feishu thất bại, vui lòng kiểm tra quyền hạn của ứng dụng");
        }
        String openId = userInfo.getString("open_id");
        String name   = userInfo.getString("name");
        String avatar = userInfo.getString("avatar_url");
        log.info("Đăng nhập OAuth2, người dùng: openId={}, name={}", openId, name);

        // Step3: Kiểm tra tính hợp lệ của tenant
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            Long count = tenantMapper.tenantIzExist(tenantId);
            if (ObjectUtil.isEmpty(count) || 0 == count) {
                throw new JeecgBootException("Tenant không tồn tại!");
            }
        }
        // Step4: Tìm hoặc tạo bản ghi tài khoản bên thứ ba
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
        queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
        queryWrapper.and(w -> w.eq(SysThirdAccount::getThirdUserUuid, openId)
                .or().eq(SysThirdAccount::getThirdUserId, openId));
        List<SysThirdAccount> accountList = sysThirdAccountService.list(queryWrapper);
        SysThirdAccount thirdAccount = accountList != null && !accountList.isEmpty() ? accountList.get(0) : null;
        if (thirdAccount == null) {
            ThirdLoginModel tlm = new ThirdLoginModel(THIRD_TYPE, openId, name, avatar);
            thirdAccount = sysThirdAccountService.saveThirdUser(tlm, tenantId);
        }
        // Step5: Kiểm tra đã liên kết người dùng hệ thống hay chưa
        if (StringUtils.isEmpty(thirdAccount.getSysUserId())) {
            throw new JeecgBootException("Tài khoản Feishu này chưa liên kết với người dùng hệ thống, vui lòng đồng bộ hoặc liên kết trước!openId=" + openId);
        }
        SysUser sysUser = sysUserService.getById(thirdAccount.getSysUserId());
        if (sysUser == null || sysUser.getStatus() == null || sysUser.getStatus() == 2) {
            throw new JeecgBootException("Người dùng hệ thống tương ứng đã bị dừng hoặc không tồn tại, vui lòng liên hệ quản trị viên");
        }
        return sysUser;
    }
    //update-end---author:liusq ---date:2026-05-15  for：【QQYUN-12767] Đăng nhập quét mã Feishu đổi sang dùng cấu hình yml, thêm phương thức nạp chồng hỗ trợ truyền cấu hình từ bên ngoài vào-----------

    /**
     * Dùng mã ủy quyền để đổi lấy user_access_token(Giao diện OIDC phiên bản mới của Feishu)
     * <p>Feishu yêu cầu:Authorization: Bearer app_access_token(lấy trước app_access_token, sau đó đổi sang token người dùng)</p>
     */
    private String getUserAccessToken(SysThirdAppConfig config, String code, String redirectUri) {
        // Step1: Lấy app_access_token(token định danh ứng dụng, khác với tenant_access_token）
        String appAccessToken = getAppAccessToken(config);
        if (appAccessToken == null) {
            log.error("Feishu lấy app_access_token thất bại, không thể đổi lấy token người dùng token");
            return null;
        }
        // Step2: Dùng app_access_token đổi lấy user_access_token
        JSONObject body = new JSONObject();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        if (StringUtils.isNotEmpty(redirectUri)) {
            body.put("redirect_uri", redirectUri);
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(USER_TOKEN_URL);
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            httpPost.setHeader("Authorization", "Bearer " + appAccessToken);
            httpPost.setEntity(new StringEntity(body.toJSONString(), ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.debug("Feishu user_access_token Phản hồi: {}", responseBody);
                JSONObject result = JSON.parseObject(responseBody);
                if (result != null && result.getIntValue("code") == 0) {
                    JSONObject data = result.getJSONObject("data");
                    return data != null ? data.getString("access_token") : null;
                }
                log.error("Feishu lấy user_access_token Thất bại: {}", responseBody);
                return null;
            }
        } catch (Exception e) {
            log.error("Feishu lấy user_access_token Ngoại lệ", e);
            return null;
        }
    }

    /**
     * Lấy cấp ứng dụng Feishu app_access_token(dùng để đổi token người dùng khi đăng nhập OAuth2)
     */
    private String getAppAccessToken(SysThirdAppConfig config) {
        JSONObject body = new JSONObject();
        body.put("app_id", config.getClientId());
        body.put("app_secret", config.getClientSecret());
        JSONObject result = doPost(APP_ACCESS_TOKEN_URL, null, body.toJSONString());
        if (result != null && result.getIntValue("code") == 0) {
            return result.getString("app_access_token");
        }
        log.error("Feishu lấy app_access_token Thất bại: {}", result);
        return null;
    }

    /**
     * Dùng user_access_token Lấy thông tin người dùng đang đăng nhập
     */
    private JSONObject getFeishuUserInfo(String userAccessToken) {
        JSONObject result = doGet(USER_INFO_URL, userAccessToken);
        if (result != null && result.getIntValue("code") == 0) {
            return result.getJSONObject("data");
        }
        log.error("Feishu lấy thông tin người dùng thất bại: {}", result);
        return null;
    }

    // ==================== Feishu→Đồng bộ danh bạ cục bộ ====================

    /**
     * Đồng bộ danh bạ Feishu về cục bộ (phòng ban + người dùng)
     * <p>
     * Quyền hạn ứng dụng Feishu cần thiết (bật trong nền tảng mở Feishu → Chi tiết ứng dụng → mở trong quản lý quyền hạn）：<br>
     * - Lấy thông tin phòng ban:contact:department:read<br>
     * - Lấy thông tin cơ bản của người dùng (Họ tên/Email/Ảnh đại diện):contact:user.base:readonly<br>
     * - Lấy số điện thoại người dùng:contact:user.phone:readonly(Đồng bộ số điện thoại phải được bật)<br>
     * - Lấy thông tin tenant (tên công ty):tenant:readable<br>
     * Nếu số điện thoại chưa được đồng bộ, vui lòng kiểm tra xem đã bật contact:user.phone:readonly quyền hạn và phát hành lại phiên bản ứng dụng.
     *
     * @param tenantId Tenant ID
     */
    public SyncInfoVo syncFeishuDeptAndUserToLocal(Integer tenantId) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        SysThirdAppConfig config = configMapper.getThirdConfigByThirdType(tenantId, THIRD_TYPE);
        if (config == null) {
            syncInfo.addFailInfo("Ứng dụng Feishu chưa được cấu hình, vui lòng cấu hình ứng dụng Feishu trong tích hợp bên thứ ba trước");
            return syncInfo;
        }
        String appToken = getTenantAccessToken(config);
        if (appToken == null) {
            syncInfo.addFailInfo("Lấy Feishu tenant_access_token Thất bại, vui lòng kiểm tra AppID và AppSecret");
            return syncInfo;
        }
        log.info("Bắt đầu đồng bộ danh bạ Feishu,tenantId={}", tenantId);

        //update-begin---author:liusq ---date:2026-06-02  for：【QQYUN-12767] Sửa vấn đề phòng ban gốc (nút công ty) và các thành viên trực thuộc của nó chưa được đồng bộ
        // Step1: Xây dựng「Feishu open_department_id → Ánh xạ SysDepart cục bộ」
        // Đồng bộ trước chính phòng ban gốc (Công ty TNHH Khoa học Công nghệ QiaoQiaoYun Bắc Kinh), sau đó lấy id cục bộ của nó làm node cha để đồng bộ đệ quy các phòng ban con
        Map<String, SysDepart> feishuDeptMap = new LinkedHashMap<>();
        SysDepart rootDepart = syncFeishuRootDept(appToken, tenantId, syncInfo);
        String rootLocalId = null;
        if (rootDepart != null) {
            feishuDeptMap.put(rootDepart.getFeishuIdentifier(), rootDepart);
            rootLocalId = rootDepart.getId();
            log.info("Đồng bộ phòng ban gốc Feishu hoàn tất：{}", rootDepart.getDepartName());
        }
        syncFeishuDepts(appToken, "0", rootLocalId, tenantId, feishuDeptMap, syncInfo);
        //update-end---author:liusq ---date:2026-06-02  for：【QQYUN-12767] Sửa vấn đề phòng ban gốc (nút công ty) và các thành viên trực thuộc của nó chưa được đồng bộ
        log.info("Đồng bộ phòng ban Feishu hoàn tất, đã xử lý tổng cộng {}  phòng ban", feishuDeptMap.size());

        // Step2: Duyệt phòng ban, đồng bộ người dùng
        Set<String> syncedOpenIds = new HashSet<>();
        for (Map.Entry<String, SysDepart> entry : feishuDeptMap.entrySet()) {
            syncFeishuDeptUsers(appToken, entry.getKey(), entry.getValue(), tenantId, syncedOpenIds, syncInfo);
        }
        log.info("Đồng bộ người dùng Feishu hoàn tất, đã xử lý tổng cộng {} người dùng", syncedOpenIds.size());

        //update-begin---author:liusq ---date:2026-05-18  for：【QQYUN-12767] Đồng bộ Feishu thêm logic xóa: phòng ban phía Feishu đã xóa/Đồng bộ người dùng dọn dẹp dữ liệu cục bộ
        // Step3: Xóa các phòng ban có feishuIdentifier ở local nhưng không còn tồn tại trong Feishu
        // Chỉ thực thi khi lần đồng bộ này không có lỗi gọi API, tránh dữ liệu bị xóa nhầm do API gặp lỗi
        if (syncInfo.getFailInfo().isEmpty()) {
            LambdaQueryWrapper<SysDepart> deptQuery = new LambdaQueryWrapper<SysDepart>()
                    .isNotNull(SysDepart::getFeishuIdentifier)
                    .ne(SysDepart::getFeishuIdentifier, "")
                    .eq(SysDepart::getDelFlag, "0");
            if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
                deptQuery.eq(SysDepart::getTenantId, tenantId);
            }
            List<SysDepart> localFeishuDepts = sysDepartService.list(deptQuery);
            for (SysDepart dept : localFeishuDepts) {
                if (!feishuDeptMap.containsKey(dept.getFeishuIdentifier())) {
                    sysDepartService.deleteDepart(dept.getId());
                    syncInfo.addSuccessInfo("Xóa phòng ban:" + dept.getDepartName());
                    log.info("Feishu đồng bộ xóa phòng ban：{}，feishuIdentifier={}", dept.getDepartName(), dept.getFeishuIdentifier());
                }
            }
        }

        // Step4: Vô hiệu hóa người dùng có gắn kết feishu ở local nhưng không còn tồn tại trong Feishu (đặt thành trạng thái vô hiệu, không xóa trực tiếp tài khoản)
        // Tương tự chỉ thực thi khi không có lỗi API
        if (syncInfo.getFailInfo().isEmpty()) {
            LambdaQueryWrapper<SysThirdAccount> accountQuery = new LambdaQueryWrapper<SysThirdAccount>()
                    .eq(SysThirdAccount::getThirdType, THIRD_TYPE)
                    .eq(SysThirdAccount::getTenantId, tenantId)
                    .isNotNull(SysThirdAccount::getThirdUserUuid)
                    .ne(SysThirdAccount::getThirdUserUuid, "");
            List<SysThirdAccount> localFeishuAccounts = sysThirdAccountService.list(accountQuery);
            for (SysThirdAccount account : localFeishuAccounts) {
                if (!syncedOpenIds.contains(account.getThirdUserUuid())
                        && StringUtils.isNotEmpty(account.getSysUserId())) {
                    SysUser user = sysUserService.getById(account.getSysUserId());
                    if (user != null && user.getStatus() != 2) {
                        user.setStatus(2);
                        sysUserService.updateById(user);
                        syncInfo.addSuccessInfo("Vô hiệu hóa người dùng đã nghỉ việc：" + user.getRealname());
                        log.info("Feishu đồng bộ vô hiệu hóa người dùng：{}，openId={}", user.getRealname(), account.getThirdUserUuid());
                    }
                }
            }
        }
        //update-end---author:liusq ---date:2026-05-18  for：【QQYUN-12767] Đồng bộ Feishu thêm logic xóa: phòng ban phía Feishu đã xóa/Đồng bộ người dùng dọn dẹp dữ liệu cục bộ

        return syncInfo;
    }

    //update-begin---author:liusq ---date:2026-06-02  for：【QQYUN-12767] Sửa vấn đề phòng ban gốc (nút công ty) và các thành viên trực thuộc của nó chưa được đồng bộ
    /**
     * Đồng bộ phòng ban gốc Feishu (tức là nút cấp cao nhất của công ty,department_id="0") về máy cục bộ.
     * <p>
     * Phòng ban gốc của Feishu sẽ không xuất hiện trong danh sách children, cần lấy riêng thông qua API chi tiết phòng ban
     * open_department_id và tên, tạo mới hoặc cập nhật bản ghi SysDepart tương ứng tại local,
     * Và lấy id cục bộ của nó làm node cha của tất cả các phòng ban con cấp một, đảm bảo cấu trúc cây ở local khớp với Feishu.
     * </p>
     */
    private SysDepart syncFeishuRootDept(String appToken, Integer tenantId, SyncInfoVo syncInfo) {
        // Feishu department_id="0" Biểu thị phòng ban gốc, sử dụng department_id Truy vấn theo loại
        String url = String.format(DEPT_DETAIL_URL, "0") + "?department_id_type=department_id";
        JSONObject resp = doGet(url, appToken);
        if (resp == null || resp.getIntValue("code") != 0) {
            String errMsg = resp != null ? resp.getString("msg") : "Lỗi mạng";
            syncInfo.addFailInfo("Lấy thông tin phòng ban gốc Feishu thất bại: " + errMsg);
            return null;
        }
        JSONObject data = resp.getJSONObject("data");
        if (data == null) {
            syncInfo.addFailInfo("Lấy thông tin phòng ban gốc Feishu, dữ liệu trả về trống");
            return null;
        }
        JSONObject deptObj = data.getJSONObject("department");
        if (deptObj == null) {
            syncInfo.addFailInfo("Lấy thông tin phòng ban gốc của Feishu, trả về trường department rỗng");
            return null;
        }
        String openDeptId = deptObj.getString("open_department_id");
        //update-begin---author:liusq ---date:2026-06-02  for：【QQYUN-12767】Phòng ban gốc ảo Feishu"0"Bản thân không có tên, lấy tên công ty theo cơ chế fallback ba cấp theo mức độ ưu tiên
        // Ưu tiên 1：department.name
        String deptName = deptObj.getString("name");
        // Ưu tiên 2：department.i18n_name.zh_cn
        if (StringUtils.isEmpty(deptName)) {
            JSONObject i18nName = deptObj.getJSONObject("i18n_name");
            if (i18nName != null) {
                deptName = i18nName.getString("zh_cn");
            }
        }
        // Ưu tiên 3: Giao diện thông tin tenant (gốc ảo"0"Khi không có tên, tên công ty được lưu trong cấu hình tenant)
        if (StringUtils.isEmpty(deptName)) {
            JSONObject tenantResp = doGet(TENANT_INFO_URL, appToken);
            if (tenantResp != null && tenantResp.getIntValue("code") == 0) {
                JSONObject tenantData = tenantResp.getJSONObject("data");
                if (tenantData != null) {
                    JSONObject tenant = tenantData.getJSONObject("tenant");
                    if (tenant != null) {
                        deptName = tenant.getString("name");
                    }
                }
            }
        }
        if (StringUtils.isEmpty(deptName)) {
            syncInfo.addFailInfo("Lấy tên phòng ban gốc Feishu thất bại:department.name、i18n_name.zh_cn Và giao diện tenant đều chưa trả về tên hợp lệ");
            return null;
        }
        //update-end---author:liusq ---date:2026-06-02  for：【QQYUN-12767】Phòng ban gốc ảo Feishu"0"Bản thân không có tên, lấy tên công ty theo cơ chế fallback ba cấp theo mức độ ưu tiên

        SysDepart localDepart = findLocalDeptByFeishuId(openDeptId, tenantId);
        if (localDepart == null) {
            localDepart = findLocalDeptByNameAndParent(deptName, null, tenantId);
        }
        if (localDepart == null) {
            localDepart = createLocalDepart(deptName, null, openDeptId, tenantId);
            if (localDepart != null) {
                syncInfo.addSuccessInfo("Thêm phòng ban gốc: " + deptName);
            } else {
                syncInfo.addFailInfo("Tạo phòng ban gốc thất bại:" + deptName);
                return null;
            }
        } else {
            boolean changed = false;
            if (!openDeptId.equals(localDepart.getFeishuIdentifier())) {
                localDepart.setFeishuIdentifier(openDeptId);
                changed = true;
            }
            if (!deptName.equals(localDepart.getDepartName())) {
                localDepart.setDepartName(deptName);
                changed = true;
            }
            if (changed) {
                sysDepartService.updateDepartDataById(localDepart, "admin");
            }
            syncInfo.addSuccessInfo("Cập nhật phòng ban gốc: " + deptName);
        }
        return localDepart;
    }
    //update-end---author:liusq ---date:2026-06-02  for：【QQYUN-12767] Sửa vấn đề phòng ban gốc (nút công ty) và các thành viên trực thuộc của nó chưa được đồng bộ

    /**
     * Đồng bộ đệ quy phòng ban Feishu về cục bộ
     */
    private void syncFeishuDepts(String appToken, String parentOpenDeptId, String localParentId,
                                  Integer tenantId, Map<String, SysDepart> feishuDeptMap, SyncInfoVo syncInfo) {
        String pageToken = null;
        do {
            String url = String.format(DEPT_CHILDREN_URL, parentOpenDeptId) + "?page_size=50&department_id_type=open_department_id";
            if (pageToken != null) {
                url += "&page_token=" + pageToken;
            }
            JSONObject resp = doGet(url, appToken);
            if (resp == null || resp.getIntValue("code") != 0) {
                String errMsg = resp != null ? resp.getString("msg") : "Lỗi mạng";
                syncInfo.addFailInfo("Lấy phòng ban Feishu[" + parentOpenDeptId + "]phòng ban con thất bại: " + errMsg);
                return;
            }
            JSONObject data = resp.getJSONObject("data");
            if (data == null) {
                break;
            }
            JSONArray items = data.getJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    JSONObject deptObj = items.getJSONObject(i);
                    String openDeptId = deptObj.getString("open_department_id");
                    String deptName   = deptObj.getString("name");

                    SysDepart localDepart = findLocalDeptByFeishuId(openDeptId, tenantId);
                    if (localDepart == null) {
                        localDepart = findLocalDeptByNameAndParent(deptName, localParentId, tenantId);
                    }
                    if (localDepart == null) {
                        localDepart = createLocalDepart(deptName, localParentId, openDeptId, tenantId);
                        if (localDepart != null) {
                            syncInfo.addSuccessInfo("Thêm phòng ban mới:" + deptName);
                        } else {
                            syncInfo.addFailInfo("Tạo phòng ban thất bại:" + deptName);
                            continue;
                        }
                    } else {
                        //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, khi cập nhật đồng thời sửa parentId và feishuIdentifier, sửa lỗi mất phân cấp phòng ban-----------
                        boolean changed = false;
                        if (!openDeptId.equals(localDepart.getFeishuIdentifier())) {
                            localDepart.setFeishuIdentifier(openDeptId);
                            changed = true;
                        }
                        if (!StringUtils.equals(localParentId, localDepart.getParentId())) {
                            localDepart.setParentId(localParentId);
                            changed = true;
                        }
                        //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, khi cập nhật đồng thời sửa parentId và feishuIdentifier, sửa lỗi mất phân cấp phòng ban-----------
                        //update-begin---author:liusq ---date:2026-05-18  for：【QQYUN-12767] Khi đồng bộ phòng ban Feishu thì bổ sung cập nhật tên; sau khi đổi tên trên Feishu, local sẽ đồng bộ cập nhật-----------
                        if (!deptName.equals(localDepart.getDepartName())) {
                            localDepart.setDepartName(deptName);
                            changed = true;
                        }
                        //update-end---author:liusq ---date:2026-05-18  for：【QQYUN-12767] Khi đồng bộ phòng ban Feishu thì bổ sung cập nhật tên; sau khi đổi tên trên Feishu, local sẽ đồng bộ cập nhật-----------
                        if (changed) {
                            if (StringUtils.isNotEmpty(localParentId)) {
                                sysDepartService.updateIzLeaf(localParentId, CommonConstant.NOT_LEAF);
                            }
                            sysDepartService.updateDepartDataById(localDepart, "admin");
                        }
                        syncInfo.addSuccessInfo("Cập nhật phòng ban:" + deptName);
                    }
                    feishuDeptMap.put(openDeptId, localDepart);
                    syncFeishuDepts(appToken, openDeptId, localDepart.getId(), tenantId, feishuDeptMap, syncInfo);
                }
            }
            boolean hasMore = Boolean.TRUE.equals(data.getBoolean("has_more"));
            pageToken = hasMore ? data.getString("page_token") : null;
        } while (pageToken != null);
    }

    /**
     * Đồng bộ người dùng trực thuộc một phòng ban Feishu về local
     */
    private void syncFeishuDeptUsers(String appToken, String openDeptId, SysDepart localDepart,
                                      Integer tenantId, Set<String> syncedOpenIds, SyncInfoVo syncInfo) {
        String pageToken = null;
        do {
            String url = DEPT_USERS_URL + "?department_id=" + openDeptId
                       + "&department_id_type=open_department_id&page_size=50&user_id_type=open_id";
            if (pageToken != null) {
                url += "&page_token=" + pageToken;
            }
            JSONObject resp = doGet(url, appToken);
            if (resp == null || resp.getIntValue("code") != 0) {
                String errMsg = resp != null ? resp.getString("msg") : "Lỗi mạng";
                syncInfo.addFailInfo("Lấy phòng ban Feishu[" + openDeptId + "]danh sách người dùng thất bại: " + errMsg);
                return;
            }
            JSONObject data = resp.getJSONObject("data");
            if (data == null) {
                break;
            }
            JSONArray items = data.getJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    JSONObject userObj = items.getJSONObject(i);
                    String openId  = userObj.getString("open_id");
                    if (syncedOpenIds.contains(openId)) {
                        if (localDepart != null) {
                            linkUserToDepart(openId, tenantId, localDepart.getId());
                        }
                        continue;
                    }
                    syncedOpenIds.add(openId);

                    String name      = userObj.getString("name");
                    String email     = userObj.getString("email");
                    String mobile    = userObj.getString("mobile");
                    String unionId   = userObj.getString("union_id");
                    JSONObject avatar = userObj.getJSONObject("avatar");
                    String avatarUrl = avatar != null ? avatar.getString("avatar_72") : null;
                    //update-begin---author:liusq ---date:2026-06-02  for：【QQYUN-12767] Khi số điện thoại rỗng thì in warn, hướng dẫn người dùng kiểm tra quyền ứng dụng Feishu
                    if (StringUtils.isEmpty(mobile)) {
                        log.warn("Người dùng Feishu[{}]Số điện thoại chưa được trả về, vui lòng xác nhận ứng dụng Feishu đã bật contact:user.phone:readonly quyền hạn và phát hành lại phiên bản ứng dụng", name);
                    }
                    //update-end---author:liusq ---date:2026-06-02  for：【QQYUN-12767] Khi số điện thoại rỗng thì in warn, hướng dẫn người dùng kiểm tra quyền ứng dụng Feishu

                    //update-begin---author:liusq ---date:2026-10-18  for：【QQYUN-12767] Khi đồng bộ từng người dùng Feishu gặp lỗi, ghi lại thông tin thất bại rồi tiếp tục, tránh một bản ghi lỗi làm gián đoạn toàn bộ lần đồng bộ-----------
                    try {
                        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
                        query.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
                        query.eq(SysThirdAccount::getTenantId, tenantId);
                        query.and(w -> w.eq(SysThirdAccount::getThirdUserUuid, openId)
                                .or().eq(SysThirdAccount::getThirdUserId, openId));
                        //update-begin---author:liusq ---date:2026-10-18  for：【QQYUN-12767] Khi tồn tại tài khoản bên thứ ba Feishu trùng lặp thì không ném TooManyResultsException nữa, ưu tiên lấy bản ghi đã gắn với người dùng hệ thống-----------
                        List<SysThirdAccount> thirdAccounts = sysThirdAccountService.list(query);
                        if (thirdAccounts.size() > 1) {
                            log.warn("Người dùng Feishu[{}]có {} bản ghi tài khoản bên thứ ba trùng lặp, vui lòng dọn dẹp sys_third_account dữ liệu trùng lặp,openId={}", name, thirdAccounts.size(), openId);
                        }
                        SysThirdAccount thirdAccount = thirdAccounts.stream()
                                .filter(a -> StringUtils.isNotEmpty(a.getSysUserId()))
                                .findFirst()
                                .orElse(thirdAccounts.isEmpty() ? null : thirdAccounts.get(0));
                        //update-end---author:liusq ---date:2026-10-18  for：【QQYUN-12767] Khi tồn tại tài khoản bên thứ ba Feishu trùng lặp thì không ném TooManyResultsException nữa, ưu tiên lấy bản ghi đã gắn với người dùng hệ thống-----------

                        //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, người dùng đã liên kết thực hiện cập nhật thông tin (họ tên/Điện thoại/Email/ảnh đại diện), sửa vấn đề người dùng đã tồn tại bị bỏ qua không cập nhật khi đồng bộ-----------
                        if (thirdAccount != null && StringUtils.isNotEmpty(thirdAccount.getSysUserId())) {
                            // Đã có quan hệ liên kết, cập nhật thông tin cơ bản của người dùng
                            SysUser existUser = sysUserService.getById(thirdAccount.getSysUserId());
                            if (existUser != null) {
                                existUser.setRealname(name);
                                if (StringUtils.isNotEmpty(email)) {
                                    existUser.setEmail(email);
                                }
                                if (StringUtils.isNotEmpty(mobile)) {
                                    existUser.setPhone(mobile.replaceAll("^\\+86\\s*", "").trim());
                                }
                                if (StringUtils.isNotEmpty(avatarUrl)) {
                                    existUser.setAvatar(avatarUrl);
                                }
                                sysUserService.updateById(existUser);
                                //update-begin---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Người dùng đồng bộ Feishu chưa liên kết với thuê bao hiện tại, bổ sung quan hệ thuê bao còn thiếu trong lịch sử đồng bộ-----------
                                createUserTenant(existUser.getId(), false, tenantId);
                                //update-end---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Người dùng đồng bộ Feishu chưa liên kết với thuê bao hiện tại, bổ sung quan hệ thuê bao còn thiếu trong lịch sử đồng bộ-----------
                            }
                            syncInfo.addSuccessInfo("Cập nhật người dùng đã tồn tại:" + name);
                            if (localDepart != null) {
                                linkUserToDepart(openId, tenantId, localDepart.getId());
                            }
                            continue;
                        }
                        //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, người dùng đã liên kết thực hiện cập nhật thông tin (họ tên/Điện thoại/Email/ảnh đại diện), sửa vấn đề người dùng đã tồn tại bị bỏ qua không cập nhật khi đồng bộ-----------

                        //update-begin---author:jeecg ---date:2026-08-04  for：【QQYUN-12767] Đồng bộ Feishu kiểm tra trùng theo số điện thoại: người dùng local đã có số điện thoại thì gắn trực tiếp, không tạo thêm người dùng-----------
                        // Logic dự phòng: không tìm thấy qua thirdUserUuid/thirdUserId Khi tìm thấy bản ghi liên kết, trước tiên khớp người dùng local theo số điện thoại
                        // Nếu trùng khớp thì trực tiếp gắn openId Feishu vào người dùng hiện có, tránh tạo trùng tài khoản (căn chỉnh theo hành vi addDepartUser của DingTalk)
                        String strippedMobile = StringUtils.isNotEmpty(mobile) ? mobile.replaceAll("^\\+86\\s*", "").trim() : null;
                        SysUser userByPhone = StringUtils.isNotEmpty(strippedMobile) ? userMapper.getUserByPhone(strippedMobile) : null;
                        if (userByPhone != null) {
                            try {
                                // 1. Cập nhật người dùng local hiện có bằng thông tin mới nhất của Feishu (chỉ cập nhật email/Ảnh đại diện; họ tên do nghiệp vụ tự duy trì, tên trên Feishu có thể không khớp với tên trong hệ thống, không ghi đè)
                                boolean userChanged = false;
                                if (StringUtils.isNotEmpty(email) && !email.equals(userByPhone.getEmail())) {
                                    userByPhone.setEmail(email);
                                    userChanged = true;
                                }
                                if (StringUtils.isNotEmpty(avatarUrl) && !avatarUrl.equals(userByPhone.getAvatar())) {
                                    userByPhone.setAvatar(avatarUrl);
                                    userChanged = true;
                                }
                                if (userChanged) {
                                    sysUserService.updateById(userByPhone);
                                }
                                // 2. Tái sử dụng hoặc tạo bản ghi tài khoản bên thứ ba, và gắn sysUserId vào người dùng hiện có đó
                                SysThirdAccount accountToBind;
                                if (thirdAccount == null) {
                                    ThirdLoginModel tlm = new ThirdLoginModel(THIRD_TYPE, openId, name, avatarUrl);
                                    accountToBind = sysThirdAccountService.saveThirdUser(tlm, tenantId);
                                } else {
                                    accountToBind = thirdAccount;
                                    accountToBind.setThirdUserUuid(openId);
                                    accountToBind.setThirdUserId(openId);
                                    accountToBind.setRealname(name);
                                    if (StringUtils.isNotEmpty(avatarUrl)) {
                                        accountToBind.setAvatar(avatarUrl);
                                    }
                                    sysThirdAccountService.updateById(accountToBind);
                                }
                                if (StringUtils.isEmpty(accountToBind.getSysUserId())
                                        || !accountToBind.getSysUserId().equals(userByPhone.getId())) {
                                    accountToBind.setSysUserId(userByPhone.getId());
                                    sysThirdAccountService.updateById(accountToBind);
                                }
                                //update-begin---author:jeecg ---date:2026-10-19  for：【QQYUN-12767] Khi đồng bộ người dùng Feishu chưa liên kết với tenant hiện tại, người dùng đã có khớp theo số điện thoại sẽ được thêm vào tenant (đang xét duyệt, căn chỉnh theo DingTalk)-----------
                                createUserTenant(userByPhone.getId(), true, tenantId);
                                //update-end---author:jeecg ---date:2026-10-19  for：【QQYUN-12767] Khi đồng bộ người dùng Feishu chưa liên kết với tenant hiện tại, người dùng đã có khớp theo số điện thoại sẽ được thêm vào tenant (đang xét duyệt, căn chỉnh theo DingTalk)-----------
                                // 3. Liên kết phòng ban
                                if (localDepart != null) {
                                    linkUserToDepart(openId, tenantId, localDepart.getId());
                                }
                                syncInfo.addSuccessInfo("Liên kết người dùng hiện có theo số điện thoại:" + name + "（" + strippedMobile + "）");
                                log.info("Đồng bộ Feishu liên kết người dùng hiện có theo số điện thoại:openId={}, phone={}, sysUserId={}",
                                        openId, strippedMobile, userByPhone.getId());
                            } catch (Exception e) {
                                log.error("Đồng bộ Feishu liên kết người dùng theo số điện thoại xảy ra ngoại lệ,name={}", name, e);
                                syncInfo.addFailInfo("Liên kết người dùng theo số điện thoại[" + name + "]Ngoại lệ:" + e.getMessage());
                            }
                            continue;
                        }
                        //update-end---author:jeecg ---date:2026-08-04  for：【QQYUN-12767] Đồng bộ Feishu kiểm tra trùng theo số điện thoại: người dùng local đã có số điện thoại thì gắn trực tiếp, không tạo thêm người dùng-----------

                        SysUser newUser = createLocalUser(name, email, mobile, openId, unionId, avatarUrl, tenantId);
                        if (newUser != null) {
                            ThirdLoginModel tlm = new ThirdLoginModel(THIRD_TYPE, openId, name, avatarUrl);
                            SysThirdAccount saved = sysThirdAccountService.saveThirdUser(tlm, tenantId);
                            saved.setSysUserId(newUser.getId());
                            sysThirdAccountService.updateById(saved);
                            //update-begin---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Người dùng đồng bộ Feishu chưa liên kết với thuê bao hiện tại, người dùng mới tạo sẽ được thêm vào thuê bao hiện tại-----------
                            createUserTenant(newUser.getId(), false, tenantId);
                            //update-end---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Người dùng đồng bộ Feishu chưa liên kết với thuê bao hiện tại, người dùng mới tạo sẽ được thêm vào thuê bao hiện tại-----------
                            if (localDepart != null) {
                                SysUserDepart userDepart = new SysUserDepart(newUser.getId(), localDepart.getId());
                                sysUserDepartService.save(userDepart);
                            }
                            syncInfo.addSuccessInfo("Người dùng mới:" + name
                                    + (StringUtils.isNotEmpty(email) ? "（" + email + "）" : ""));
                        } else {
                            syncInfo.addFailInfo("Tạo người dùng thất bại:" + name);
                        }
                    } catch (Exception e) {
                        log.error("Feishu đồng bộ người dùng bất thường,name={}, openId={}", name, openId, e);
                        syncInfo.addFailInfo("Đồng bộ người dùng[" + name + "]Ngoại lệ:" + e.getMessage());
                    }
                    //update-end---author:liusq ---date:2026-10-19  for：【QQYUN-12767] Khi đồng bộ từng người dùng Feishu gặp lỗi, ghi lại thông tin thất bại rồi tiếp tục, tránh một bản ghi lỗi làm gián đoạn toàn bộ lần đồng bộ-----------
                }
            }
            boolean hasMore = Boolean.TRUE.equals(data.getBoolean("has_more"));
            pageToken = hasMore ? data.getString("page_token") : null;
        } while (pageToken != null);
    }

    // ==================== Phương thức hỗ trợ thao tác dữ liệu cục bộ ====================

    private SysDepart findLocalDeptByFeishuId(String openDeptId, Integer tenantId) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, đổi sang truy vấn bằng trường feishuIdentifier, giống như WeChat Doanh nghiệp/Đồng nhất với DingTalk-----------
        query.eq(SysDepart::getFeishuIdentifier, openDeptId);
        //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, đổi sang truy vấn bằng trường feishuIdentifier, giống như WeChat Doanh nghiệp/Đồng nhất với DingTalk-----------
        query.eq(SysDepart::getDelFlag, "0");
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            query.eq(SysDepart::getTenantId, tenantId);
        }
        return sysDepartService.getOne(query, false);
    }

    private SysDepart findLocalDeptByNameAndParent(String departName, String parentId, Integer tenantId) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.eq(SysDepart::getDepartName, departName);
        query.eq(SysDepart::getDelFlag, "0");
        if (StringUtils.isNotEmpty(parentId)) {
            query.eq(SysDepart::getParentId, parentId);
        } else {
            query.isNull(SysDepart::getParentId);
        }
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            query.eq(SysDepart::getTenantId, tenantId);
        }
        List<SysDepart> list = sysDepartService.list(query);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    private SysDepart createLocalDepart(String departName, String parentId, String openDeptId, Integer tenantId) {
        try {
            SysDepart depart = new SysDepart();
            depart.setDepartName(departName);
            depart.setParentId(parentId);
            depart.setStatus("1");
            //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, đổi sang dùng trường feishuIdentifier để lưu trữ phòng ban ID-----------
            depart.setFeishuIdentifier(openDeptId);
            //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, đổi sang dùng trường feishuIdentifier để lưu trữ phòng ban ID-----------
            depart.setTenantId(tenantId);
            // Phòng ban gốc (không có cấp cha) đặt thành loại công ty, phòng ban con đặt thành loại tổ chức, đồng bộ với DingTalk/Giữ đồng bộ nhất quán với WeChat Doanh nghiệp
            if (StringUtils.isEmpty(parentId)) {
                depart.setOrgCategory("1");
            } else {
                depart.setOrgCategory("2");
            }
            //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, dùng saveDepartData để tạo đúng orgCode và cấu trúc cây phân cấp, sửa lỗi mất phân cấp phòng ban sau khi đồng bộ-----------
            // saveDepartData Sẽ tự động tạo orgCode, orgType, izLeaf và cập nhật izLeaf của node cha, đảm bảo cấu trúc cây đúng
            // Nếu cùng cấp tồn tại orgCode có định dạng bất thường khiến saveDepartData ném ngoại lệ thì quay lại lưu bằng cách đặt giá trị thủ công, đảm bảo parentId/izLeaf Ít nhất là chính xác
            try {
                sysDepartService.saveDepartData(depart, "admin");
            } catch (Exception saveEx) {
                log.warn("saveDepartData Tạo orgCode thất bại (có thể tồn tại phòng ban cùng cấp có định dạng bất thường), quay lại lưu thủ công,departName={}", departName, saveEx);
                depart.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getIdStr(depart));
                depart.setOrgCode("FS" + System.currentTimeMillis() % 100000);
                depart.setOrgType(StringUtils.isNotEmpty(parentId) ? "2" : "1");
                depart.setIzLeaf(org.jeecg.common.constant.CommonConstant.IS_LEAF);
                depart.setCreateTime(new Date());
                depart.setDelFlag("0");
                if (StringUtils.isNotEmpty(parentId)) {
                    sysDepartService.updateIzLeaf(parentId, org.jeecg.common.constant.CommonConstant.NOT_LEAF);
                }
                sysDepartService.save(depart);
            }
            //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, dùng saveDepartData để tạo đúng orgCode và cấu trúc cây phân cấp, sửa lỗi mất phân cấp phòng ban sau khi đồng bộ-----------
            return depart;
        } catch (Exception e) {
            log.error("Tạo phòng ban local thất bại,departName={}", departName, e);
            return null;
        }
    }

    private void linkUserToDepart(String openId, Integer tenantId, String localDeptId) {
        try {
            LambdaQueryWrapper<SysThirdAccount> q = new LambdaQueryWrapper<>();
            q.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            q.eq(SysThirdAccount::getTenantId, tenantId);
            q.eq(SysThirdAccount::getThirdUserUuid, openId);
            SysThirdAccount ta = sysThirdAccountService.getOne(q, false);
            if (ta == null || StringUtils.isEmpty(ta.getSysUserId())) {
                return;
            }
            LambdaQueryWrapper<SysUserDepart> existQuery = new LambdaQueryWrapper<>();
            existQuery.eq(SysUserDepart::getUserId, ta.getSysUserId());
            existQuery.eq(SysUserDepart::getDepId, localDeptId);
            if (sysUserDepartService.count(existQuery) == 0) {
                SysUserDepart ud = new SysUserDepart(ta.getSysUserId(), localDeptId);
                sysUserDepartService.save(ud);
            }
        } catch (Exception e) {
            log.warn("Bổ sung liên kết phòng ban của người dùng thất bại,openId={}, deptId={}", openId, localDeptId, e);
        }
    }

    //update-begin---author:jeecg ---date:2026-10-16  for：【QQYUN-12767] Đồng bộ người dùng Feishu chưa liên kết tenant hiện tại, bổ sung ghi sys_user_tenant-----------
    /**
     * Thêm người dùng vào tenant (sys_user_tenant), nếu quan hệ đã tồn tại thì bỏ qua; khi chưa bật đa tenant thì không xử lý.
     * <p>Đồng bộ với DingTalk / Logic createUserTenant trong đồng bộ WeChat Work.</p>
     *
     * @param userId      Người dùng hệ thống ID
     * @param underReview true=Đang xét duyệt (người dùng đã có được khớp theo số điện thoại, cần quản trị viên tenant xét duyệt, căn chỉnh theo DingTalk);false=Bình thường
     * @param tenantId    Tenant ID
     */
    private void createUserTenant(String userId, boolean underReview, Integer tenantId) {
        if (!MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL || StringUtils.isEmpty(userId)) {
            return;
        }
        Integer count = userTenantMapper.userTenantIzExist(userId, tenantId);
        if (count == null || count == 0) {
            SysUserTenant userTenant = new SysUserTenant();
            userTenant.setTenantId(tenantId);
            userTenant.setUserId(userId);
            userTenant.setStatus(underReview ? CommonConstant.USER_TENANT_UNDER_REVIEW : CommonConstant.USER_TENANT_NORMAL);
            userTenantMapper.insert(userTenant);
        }
    }
    //update-end---author:jeecg ---date:2026-10-16  for：【QQYUN-12767] Đồng bộ người dùng Feishu chưa liên kết tenant hiện tại, bổ sung ghi sys_user_tenant-----------

    private SysUser createLocalUser(String name, String email, String mobile,
                                     String openId, String unionId, String avatar, Integer tenantId) {
        String username = "feishu_" + (openId.length() > 8 ? openId.substring(openId.length() - 8) : openId);
        if (sysUserService.getUserByName(username) != null) {
            username = username + "_" + oConvertUtils.randomGen(4);
        }
        String salt = oConvertUtils.randomGen(8);
        String password = PasswordUtil.encrypt(username, oConvertUtils.randomGen(10), salt);
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setRealname(name);
        user.setPassword(password);
        user.setSalt(salt);
        user.setEmail(email);
        user.setPhone(StringUtils.isNotEmpty(mobile) ? mobile.replaceAll("^\\+86\\s*", "").trim() : null);
        user.setAvatar(avatar);
        user.setStatus(1);
        user.setDelFlag(0);
        user.setActivitiSync(1);
        user.setSort(CommonConstant.DEFAULT_USER_SORT);
        try {
            sysUserService.addUserWithRole(user, "");
            return user;
        } catch (Exception e) {
            log.error("Tạo người dùng local thất bại,name={}", name, e);
            return null;
        }
    }

    // ==================== Phương thức tiện ích API Feishu ====================

    //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, thống nhất lấy cấu hình Feishu; khi chưa bật đa thuê bao thì dùng thuê bao mặc định-----------
    /**
     * Lấy cấu hình Feishu theo tenant hiện tại.
     * <p>Chưa bật đa tenant (OPEN_SYSTEM_TENANT_CONTROL=false) khi đó, cố định dùng tenantId=0 truy vấn cấu hình mặc định,
     * Đồng bộ với DingTalk getDingThirdAppConfig() / WeCom getWeChatThirdAppConfig() cách xử lý.</p>
     * <p>Nếu trong cơ sở dữ liệu không tồn tại cấu hình thì fallback sang mục cấu hình FEISHU trong justauth yml (client-id/client-secret）。</p>
     */
    private SysThirdAppConfig getFeishuThirdAppConfig() {
        int tenantId = MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL
                ? oConvertUtils.getInt(TenantContext.getTenant(), 0)
                : 0;
        SysThirdAppConfig config = configMapper.getThirdConfigByThirdType(tenantId, THIRD_TYPE);
        //update-begin---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Tích hợp Feishu, khi cơ sở dữ liệu không có cấu hình thì fallback sang mục cấu hình yml-----------
        if (config == null) {
            config = buildFeishuConfigFromYaml();
        }
        //update-end---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Tích hợp Feishu, khi cơ sở dữ liệu không có cấu hình thì fallback sang mục cấu hình yml-----------
        return config;
    }

    /**
     * Đọc Feishu từ cấu hình justauth yml app_id/app_secret, xây dựng SysThirdAppConfig tạm thời.
     * client-id Trống hoặc là placeholder "??"  sẽ trả về null.
     */
    private SysThirdAppConfig buildFeishuConfigFromYaml() {
        if (justAuthProperties == null || justAuthProperties.getType() == null) {
            return null;
        }
        AuthConfig authConfig = justAuthProperties.getType().get(CommonConstant.FEISHU);
        if (authConfig == null || authConfig.getClientId() == null || "??".equals(authConfig.getClientId().trim())) {
            return null;
        }
        SysThirdAppConfig config = new SysThirdAppConfig();
        config.setClientId(authConfig.getClientId());
        config.setClientSecret(authConfig.getClientSecret());
        config.setTenantId(CommonConstant.TENANT_ID_DEFAULT_VALUE);
        return config;
    }
    //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, thống nhất lấy cấu hình Feishu; khi chưa bật đa thuê bao thì dùng thuê bao mặc định-----------

    //update-begin---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Tích hợp Feishu, thêm phương thức tiện ích chuyển HTML sang rich text post của Feishu-----------
    /**
     * Chuyển đổi rich text HTML thành cấu trúc thẻ nhiều dòng của tin nhắn post Feishu (contentRows).
     * <p>
     * Hỗ trợ quy tắc chuyển đổi:
     * <ul>
     *   <li>{@code <br>}、{@code </p>}、{@code </div>}、{@code </li>} → Xuống dòng (thêm một dòng mới)</li>
     *   <li>{@code <b>}、{@code <strong>} → Văn bản in đậm</li>
     *   <li>{@code <i>}、{@code <em>} → Văn bản in nghiêng</li>
     *   <li>{@code <u>} → Văn bản gạch dưới</li>
     *   <li>{@code <a href="...">} → Siêu liên kết (tag: a）</li>
     *   <li>{@code <li>} → Tự động thêm ký hiệu đầu mục ở phía trước "• "</li>
     *   <li>Các thẻ chưa nhận dạng còn lại: bóc thẻ, giữ lại nội dung văn bản</li>
     *   <li>HTML Thực thể:{@code &nbsp;} → khoảng trắng，{@code &amp;} → &，{@code &lt;} → <，{@code &gt;} → ></li>
     * </ul>
     *
     * @param html Chuỗi HTML gốc
     * @return Cấu trúc nhiều dòng của Feishu post content (mỗi dòng là một JSONArray, trong dòng là các thẻ JSONObject)
     */
    private JSONArray htmlToFeishuPostRows(String html) {
        JSONArray rows = new JSONArray();
        if (html == null || html.trim().isEmpty()) {
            return rows;
        }
        // Step1: Chuyển đổi thẻ xuống dòng cấp khối thành \n，<li> Thêm ký hiệu đầu mục
        String normalized = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p\\s*>", "\n")
                .replaceAll("(?i)</div\\s*>", "\n")
                .replaceAll("(?i)</li\\s*>", "\n")
                .replaceAll("(?i)<li[^>]*>", "• ");
        // Step2: Cắt dòng theo ký tự xuống dòng, phân tích từng dòng thẻ nội tuyến
        String[] lines = normalized.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            JSONArray row = parseInlineHtml(line);
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }
        // Phương án dự phòng: nếu kết quả phân tích rỗng, loại bỏ tất cả các thẻ rồi xuất ra dưới dạng văn bản thuần một dòng
        if (rows.isEmpty()) {
            String plain = html.replaceAll("<[^>]+>", "")
                    .replaceAll("&nbsp;", " ").replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<").replaceAll("&gt;", ">").trim();
            if (!plain.isEmpty()) {
                JSONArray fallbackRow = new JSONArray();
                addFeishuTextTag(fallbackRow, plain, false, false, false);
                rows.add(fallbackRow);
            }
        }
        return rows;
    }

    /**
     * Phân tích các thẻ nội tuyến trong một dòng HTML, trả về danh sách tag Feishu của dòng đó.
     * Xử lý:{@code <b>/<strong>}、{@code <i>/<em>}、{@code <u>}、{@code <a href>}, các thẻ còn lại sẽ bị bỏ qua.
     */
    private JSONArray parseInlineHtml(String line) {
        JSONArray row = new JSONArray();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(<[^>]+>|[^<]+)", java.util.regex.Pattern.DOTALL)
                .matcher(line);
        boolean bold = false, italic = false, underline = false;
        boolean insideLink = false;
        String hrefPending = null;
        StringBuilder linkText = new StringBuilder();
        while (m.find()) {
            String token = m.group();
            if (token.charAt(0) == '<') {
                // HTML Nhãn
                String lower = token.toLowerCase().trim();
                if (lower.equals("<b>") || lower.equals("<strong>") || lower.startsWith("<strong ")) {
                    bold = true;
                } else if (lower.equals("</b>") || lower.equals("</strong>")) {
                    bold = false;
                } else if (lower.equals("<i>") || lower.equals("<em>") || lower.startsWith("<em ")) {
                    italic = true;
                } else if (lower.equals("</i>") || lower.equals("</em>")) {
                    italic = false;
                } else if (lower.equals("<u>")) {
                    underline = true;
                } else if (lower.equals("</u>")) {
                    underline = false;
                } else if (lower.startsWith("<a ")) {
                    java.util.regex.Matcher hrefM = java.util.regex.Pattern
                            .compile("href=[\"']([^\"']*)[\"']", java.util.regex.Pattern.CASE_INSENSITIVE)
                            .matcher(token);
                    hrefPending = hrefM.find() ? hrefM.group(1) : null;
                    insideLink = true;
                    linkText.setLength(0);
                } else if (lower.equals("</a>")) {
                    if (insideLink) {
                        String lt = linkText.toString();
                        if (hrefPending != null && !lt.isEmpty()) {
                            JSONObject tag = new JSONObject();
                            tag.put("tag", "a");
                            tag.put("text", lt);
                            tag.put("href", hrefPending);
                            row.add(tag);
                        } else {
                            // Không có href, hạ cấp thành văn bản thường
                            addFeishuTextTag(row, lt, bold, italic, underline);
                        }
                    }
                    insideLink = false;
                    hrefPending = null;
                    linkText.setLength(0);
                }
                // Các thẻ còn lại: bỏ qua bản thân thẻ, nội dung văn bản sẽ được xử lý ở lần text token tiếp theo
            } else {
                // Đoạn văn bản thuần, thực hiện giải mã thực thể HTML đơn giản
                String text = token
                        .replaceAll("&nbsp;", " ").replaceAll("&amp;", "&")
                        .replaceAll("&lt;", "<").replaceAll("&gt;", ">");
                if (insideLink) {
                    linkText.append(text);
                } else if (!text.isEmpty()) {
                    addFeishuTextTag(row, text, bold, italic, underline);
                }
            }
        }
        return row;
    }

    /**
     * Thêm một text tag vào dòng (row) post của Feishu, đặt theo nhu cầu bold/italic/underline。
     */
    private void addFeishuTextTag(JSONArray row, String text, boolean bold, boolean italic, boolean underline) {
        if (text == null || text.isEmpty()) {
            return;
        }
        JSONObject tag = new JSONObject();
        tag.put("tag", "text");
        tag.put("text", text);
        if (bold) {
            tag.put("bold", true);
        }
        if (italic) {
            tag.put("italic", true);
        }
        if (underline) {
            tag.put("underline", true);
        }
        row.add(tag);
    }
    //update-end---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Tích hợp Feishu, thêm phương thức tiện ích chuyển HTML sang rich text post của Feishu-----------

    /**
     * Lấy cấp ứng dụng Feishu tenant_access_token(dùng cho các giao diện danh tính ứng dụng như đồng bộ danh bạ, gửi tin nhắn)
     */
    public String getTenantAccessToken(SysThirdAppConfig config) {
        if (config == null) {
            return null;
        }
        JSONObject body = new JSONObject();
        body.put("app_id", config.getClientId());
        body.put("app_secret", config.getClientSecret());
        JSONObject result = doPost(APP_TOKEN_URL, null, body.toJSONString());
        if (result != null && result.getIntValue("code") == 0) {
            return result.getString("tenant_access_token");
        }
        log.error("Feishu lấy tenant_access_token Thất bại: {}", result);
        return null;
    }

    /**
     * Gửi tới người dùng Feishu được chỉ định (open_id) gửi tin nhắn văn bản
     */
    private boolean doSendTextMessage(String appToken, String openId, String content) {
        String url = SEND_MSG_URL + "?receive_id_type=open_id";
        JSONObject msgContent = new JSONObject();
        msgContent.put("text", content);
        JSONObject body = new JSONObject();
        body.put("receive_id", openId);
        body.put("msg_type", "text");
        body.put("content", msgContent.toJSONString());
        JSONObject result = doPost(url, appToken, body.toJSONString());
        if (result != null && result.getIntValue("code") == 0) {
            log.info("Gửi tin nhắn Feishu thành công: openId={}", openId);
            return true;
        }
        log.error("Gửi tin nhắn Feishu thất bại: openId={}, result={}", openId, result);
        return false;
    }

    //update-begin---author:scott ---date:2026-10-09  for: [QiaoQiaoYun] JianLiu — thông báo tích hợp Feishu, nội dung Markdown đổi sang gửi tin nhắn dạng thẻ, tránh hiển thị văn bản Markdown gốc trong Feishu-----------
    /**
     * Gửi một tin nhắn thẻ Markdown (msg_type=interactive, nội dung được hiển thị bởi component markdown trong thẻ)
     *
     * @param title Tiêu đề thẻ, khi trống thì không hiển thị thanh tiêu đề
     */
    private boolean doSendMarkdownCardMessage(String appToken, String openId, String title, String content) {
        String url = SEND_MSG_URL + "?receive_id_type=open_id";
        JSONObject card = new JSONObject();
        JSONObject cardConfig = new JSONObject();
        cardConfig.put("wide_screen_mode", true);
        card.put("config", cardConfig);
        if (StringUtils.isNotEmpty(title)) {
            JSONObject titleObj = new JSONObject();
            titleObj.put("tag", "plain_text");
            titleObj.put("content", title);
            JSONObject header = new JSONObject();
            header.put("title", titleObj);
            header.put("template", "blue");
            card.put("header", header);
        }
        JSONObject markdown = new JSONObject();
        markdown.put("tag", "markdown");
        markdown.put("content", content);
        JSONArray elements = new JSONArray();
        elements.add(markdown);
        card.put("elements", elements);
        JSONObject body = new JSONObject();
        body.put("receive_id", openId);
        body.put("msg_type", "interactive");
        body.put("content", card.toJSONString());
        JSONObject result = doPost(url, appToken, body.toJSONString());
        if (result != null && result.getIntValue("code") == 0) {
            log.info("Gửi tin nhắn thẻ Feishu thành công: openId={}", openId);
            return true;
        }
        log.error("Gửi tin nhắn thẻ Feishu thất bại: openId={}, result={}", openId, result);
        return false;
    }
    //update-end---author:scott ---date:2026-10-09  for: [QiaoQiaoYun] JianLiu — thông báo tích hợp Feishu, nội dung Markdown đổi sang gửi tin nhắn dạng thẻ, tránh hiển thị văn bản Markdown gốc trong Feishu-----------

    // ==================== HTTP Phương thức tiện ích (dùng cho đồng bộ/sử dụng tin nhắn）====================

    /**
     * POST yêu cầu (JSON body), khi bearerToken rỗng thì không mang theo header Authorization
     */
    private JSONObject doPost(String url, String bearerToken, String jsonBody) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            if (StringUtils.isNotEmpty(bearerToken)) {
                httpPost.setHeader("Authorization", "Bearer " + bearerToken);
            }
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.debug("Feishu POST {} Phản hồi: {}", url, responseBody);
                return JSON.parseObject(responseBody);
            }
        } catch (Exception e) {
            log.error("Yêu cầu POST Feishu bất thường: url={}", url, e);
            return null;
        }
    }

    //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, thêm phương thức gửi tin nhắn thông báo, căn chỉnh theo sendActionCardMessage của DingTalk và WeChat Doanh nghiệp sendTextCardMessage-----------
    /**
     * Gửi tin nhắn thông báo tới người dùng Feishu (dạng rich text post), đồng bộ với DingTalk sendActionCardMessage / WeCom sendTextCardMessage
     *
     * @param announcement  Đối tượng thông báo
     * @param mobileOpenUrl Liên kết chuyển hướng trên thiết bị di động (có thể là null)
     * @param verifyConfig  true khi cấu hình chưa sẵn sàng thì bỏ qua trong im lặng
     * @return Có gửi thành công tất cả không
     */
    public boolean sendAnnouncementMessage(SysAnnouncement announcement, String mobileOpenUrl, boolean verifyConfig) {
        //update-begin---author:jeecg ---date:2026-05-15  for：【QQYUN-12767】Tích hợp Feishu, đổi sang dùng phương pháp lấy cấu hình thống nhất-----------
        int tenantId = MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL
                ? oConvertUtils.getInt(TenantContext.getTenant(), 0) : 0;
        SysThirdAppConfig config = getFeishuThirdAppConfig();
        //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767】Tích hợp Feishu, đổi sang dùng phương pháp lấy cấu hình thống nhất-----------
        if (config == null) {
            if (verifyConfig) {
                log.debug("Ứng dụng Feishu chưa được cấu hình, bỏ qua việc gửi tin nhắn thông báo!");
            }
            return false;
        }
        String appToken = getTenantAccessToken(config);
        if (appToken == null) {
            log.error("Feishu lấy tenant_access_token thất bại, việc gửi thông báo đã bị hủy");
            return false;
        }
        // Xác định văn bản tóm tắt
        String summary;
        if (oConvertUtils.isNotEmpty(announcement.getMsgAbstract())) {
            String msgAbstract = announcement.getMsgAbstract().trim();
            // Nếu tóm tắt là tham số mở rộng nghiệp vụ dạng JSON thì lấy nội dung chính của thông báo
            summary = (msgAbstract.startsWith("{") && msgAbstract.endsWith("}"))
                    ? announcement.getMsgContent() : msgAbstract;
        } else {
            summary = oConvertUtils.getString(announcement.getMsgContent(), "");
        }
        //update-begin---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Sửa tin nhắn thông báo Feishu: hai cách xây dựng nội dung, có thể chọn theo nhu cầu-----------
        // -----------------------------------------------------------------------
        // Cách 1 (văn bản thuần, hiện đang chú thích): loại bỏ tất cả các thẻ HTML rồi gửi dưới dạng văn bản thuần một dòng.
        //   Ưu điểm: triển khai đơn giản, tương thích tốt nhất.
        //   Nhược điểm: Mất định dạng rich text như in đậm, xuống dòng, siêu liên kết.
        //   Cách bật: bỏ ghi chú ở đoạn dưới, đồng thời ghi chú lại"Cách 2"Khối mã.
        // -----------------------------------------------------------------------
        // String plainSummary = summary.replaceAll("<[^>]+>", "").trim();
        // JSONArray contentRows = new JSONArray();
        // JSONArray textRow = new JSONArray();
        // JSONObject textTag = new JSONObject();
        // textTag.put("tag", "text");
        // textTag.put("text", plainSummary);
        // textRow.add(textTag);
        // contentRows.add(textRow);

        // -----------------------------------------------------------------------
        // Cách 2 (chuyển HTML sang rich text post của Feishu, hiện đang bật): phân tích HTML thành cấu trúc thẻ nhiều dòng của post Feishu.
        //   Hỗ trợ:<br>/<p> xuống dòng、<b>/<strong> in đậm、<i>/<em> in nghiêng、<u> Gạch dưới,<a href> Siêu liên kết.
        //   Các thẻ chưa nhận dạng còn lại: bóc thẻ, giữ lại nội dung văn bản.
        //   Cách bật: giữ nguyên đoạn mã bên dưới; nếu chuyển lại sang cách một thì chú thích lại cách này và bỏ chú thích của cách một.
        // -----------------------------------------------------------------------
        JSONArray contentRows = htmlToFeishuPostRows(summary);

        // Thêm dòng liên kết chuyển hướng (dùng chung cho hai cách)
        String linkUrl = oConvertUtils.isNotEmpty(mobileOpenUrl) ? mobileOpenUrl
                : oConvertUtils.getString(announcement.getOpenPage(), "");
        if (oConvertUtils.isNotEmpty(linkUrl)) {
            JSONArray linkRow = new JSONArray();
            JSONObject linkTag = new JSONObject();
            linkTag.put("tag", "a");
            linkTag.put("text", "Xem chi tiết");
            linkTag.put("href", linkUrl);
            linkRow.add(linkTag);
            contentRows.add(linkRow);
        }
        JSONObject zhContent = new JSONObject();
        zhContent.put("title", oConvertUtils.getString(announcement.getTitile(), "Thông báo hệ thống"));
        zhContent.put("content", contentRows);
        // API Feishu:msg_type=post thì cấu trúc trường content là {"zh_cn":{"title":"...","content":[[...]]}}
        JSONObject contentWrapper = new JSONObject();
        contentWrapper.put("zh_cn", zhContent);
        //update-end---author:jeecg ---date:2026-05-18  for：【QQYUN-12767] Sửa tin nhắn thông báo Feishu: hai cách xây dựng nội dung, có thể chọn theo nhu cầu-----------

        // Xác định người nhận
        boolean isToAll = CommonConstant.MSG_TYPE_ALL.equals(announcement.getMsgType());
        List<String> openIds = new ArrayList<>();
        if (!isToAll) {
            String[] userIds = null;
            String userId = announcement.getUserIds();
            if (oConvertUtils.isNotEmpty(userId)) {
                userIds = userId.substring(0, userId.length() - 1).split(",");
            } else {
                LambdaQueryWrapper<SysAnnouncementSend> q = new LambdaQueryWrapper<>();
                q.eq(SysAnnouncementSend::getAnntId, announcement.getId());
                SysAnnouncementSend send = sysAnnouncementSendMapper.selectOne(q);
                if (send != null) {
                    userIds = new String[]{send.getUserId()};
                }
            }
            if (userIds != null) {
                LambdaQueryWrapper<SysUser> uq = new LambdaQueryWrapper<>();
                uq.in(SysUser::getId, (Object[]) userIds);
                List<SysUser> userList = userMapper.selectList(uq);
                LambdaQueryWrapper<SysThirdAccount> tq = new LambdaQueryWrapper<>();
                tq.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
                tq.eq(SysThirdAccount::getTenantId, tenantId);
                tq.in(SysThirdAccount::getSysUserId,
                        userList.stream().map(SysUser::getId).collect(java.util.stream.Collectors.toList()));
                List<SysThirdAccount> accounts = sysThirdAccountService.list(tq);
                accounts.stream()
                        .map(SysThirdAccount::getThirdUserUuid)
                        .filter(oConvertUtils::isNotEmpty)
                        .forEach(openIds::add);
            }
        }
        if (isToAll) {
            log.warn("Feishu tạm thời chưa hỗ trợ phát thông báo toàn thể, bỏ qua gửi toAll");
            return false;
        }
        boolean allSuccess = true;
        String msgUrl = SEND_MSG_URL + "?receive_id_type=open_id";
        for (String openId : openIds) {
            JSONObject body = new JSONObject();
            body.put("receive_id", openId);
            body.put("msg_type", "post");
            body.put("content", contentWrapper.toJSONString());
            JSONObject result = doPost(msgUrl, appToken, body.toJSONString());
            if (result == null || result.getIntValue("code") != 0) {
                log.error("Gửi tin nhắn thông báo Feishu thất bại: openId={}, result={}", openId, result);
                allSuccess = false;
            } else {
                log.info("Gửi tin nhắn thông báo Feishu thành công: openId={}", openId);
            }
        }
        return allSuccess;
    }
    //update-end---author:jeecg ---date:2026-05-15  for：【QQYUN-12767] Tích hợp Feishu, thêm phương thức gửi tin nhắn thông báo, căn chỉnh theo sendActionCardMessage của DingTalk và WeChat Doanh nghiệp sendTextCardMessage-----------

    /**
     * GET yêu cầu, khi bearerToken rỗng thì không mang theo header Authorization
     */
    private JSONObject doGet(String url, String bearerToken) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json; charset=utf-8");
            if (StringUtils.isNotEmpty(bearerToken)) {
                httpGet.setHeader("Authorization", "Bearer " + bearerToken);
            }
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.debug("Feishu GET {} Phản hồi: {}", url, responseBody);
                return JSON.parseObject(responseBody);
            }
        } catch (Exception e) {
            log.error("Yêu cầu GET Feishu gặp ngoại lệ: url={}", url, e);
            return null;
        }
    }
}
//update-end---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-----------
