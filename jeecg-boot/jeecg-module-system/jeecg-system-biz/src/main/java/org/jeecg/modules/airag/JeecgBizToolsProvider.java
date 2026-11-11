package org.jeecg.modules.airag;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.llm.handler.JeecgToolsProvider;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * for [QQYUN-13565]【Trợ lý AI】Mở rộng công cụ tạo người dùng và truy vấn người dùng mới
 * @Description: jeecg llm Nhà cung cấp công cụ
 * @Author: chenrui
 * @Date: 2025/8/26 18:06
 */
@Slf4j
@Component
// AI Nhà cung cấp công cụ: chỉ khi classpath tồn tại module airag(JeecgToolsProvider API)sẽ được lắp ráp, sau khi gỡ phụ thuộc AI sẽ bỏ qua đăng ký, không ảnh hưởng đến khởi động ứng dụng
@ConditionalOnClass(name = "org.jeecg.modules.airag.llm.handler.JeecgToolsProvider")
public class JeecgBizToolsProvider implements JeecgToolsProvider {

    @Autowired
    SysUserMapper userMapper;

    @Autowired
    private BaseCommonService baseCommonService;

    @Autowired
    private org.jeecg.modules.system.service.ISysRoleService sysRoleService;

    @Autowired
    private org.jeecg.modules.system.service.ISysUserRoleService sysUserRoleService;

    @Autowired
    private org.jeecg.modules.system.service.ISysUserService sysUserService;

    @Lazy
    @Autowired
    private CommonAPI commonApi;

    @Autowired
    private RedisUtil redisUtil;

    public Map<ToolSpecification, ToolExecutor> getDefaultTools() {
       log.info("--------【Công cụ AI】Mở rộng công cụ tạo người dùng và truy vấn người dùng------------------");
        Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();

        // Đổi sang phân tích tên người dùng từ JWT của HTTP request hiện tại, rồi tra cứu tập hợp mã quyền theo userId để xác thực quyền.
        Set<String> perms = currentUserPermissions();
        if (perms.isEmpty()) {
            // Truy cập ẩn danh hoặc trạng thái đăng nhập không hợp lệ sẽ không lộ bất kỳ công cụ nghiệp vụ nào
            log.info("【Công cụ AI】Hiện tại là ẩn danh/Phiên chưa đăng nhập, bỏ qua việc đăng ký tất cả công cụ nghiệp vụ");
            return tools;
        }

        if (perms.contains("system:user:listAll")) {
            // Công cụ truy vấn tất cả người dùng
            JeecgLlmTools userTool = queryUserTool(perms);
            tools.put(userTool.getToolSpecification(), userTool.getToolExecutor());
        } else {
            log.info("【Công cụ AI】Người dùng hiện tại không có system:user:listAll quyền hạn, bỏ qua query_user_by_name đăng ký công cụ");
        }

        if (perms.contains("system:user:add")) {
            // Công cụ thêm người dùng
            JeecgLlmTools addUser = addUserTool(perms);
            tools.put(addUser.getToolSpecification(), addUser.getToolExecutor());
        } else {
            log.info("【Công cụ AI】Người dùng hiện tại không có system:user:add quyền hạn, bỏ qua add_user đăng ký công cụ");
        }

        if (perms.contains("system:role:list")) {
            // Truy vấn tất cả vai trò
            JeecgLlmTools queryRoles = queryAllRolesTool(perms);
            tools.put(queryRoles.getToolSpecification(), queryRoles.getToolExecutor());
        } else {
            log.info("【Công cụ AI】Người dùng hiện tại không có system:role:list quyền hạn, bỏ qua query_all_roles đăng ký công cụ");
        }

        if (perms.contains("system:user:addUserRole")) {
            // Cấp vai trò cho người dùng
            JeecgLlmTools grantRoles = grantUserRolesTool(perms);
            tools.put(grantRoles.getToolSpecification(), grantRoles.getToolExecutor());
        } else {
            log.info("【Công cụ AI】Người dùng hiện tại không có system:user:addUserRole quyền hạn, bỏ qua grant_user_roles đăng ký công cụ");
        }

        return tools;
    }

    /**
     * Phân tích tập hợp mã quyền hạn của người dùng tương ứng với JWT của yêu cầu HTTP hiện tại.
     * Vì /airag/chat/send Điểm vào đã được đánh dấu @IgnoreAuth, bộ lọc Shiro bị bỏ qua,
     * Subject của luồng hiện tại là ẩn danh, không thể dùng SecurityUtils.getSubject().isPermitted(...) Xác thực quyền.
     *
     * @return Tập hợp mã quyền của người dùng đang đăng nhập; khi chưa đăng nhập, token không hợp lệ hoặc phân tích thất bại thì trả về tập hợp rỗng
     */
    private Set<String> currentUserPermissions() {
        try {
            HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
            if (req == null) {
                log.info("[Công cụ AI] Chưa lấy được bối cảnh HTTP request hiện tại, xử lý theo dạng ẩn danh (không có quyền công cụ nghiệp vụ)");
                return Collections.emptySet();
            }
            //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-9qp4-7428-wvm7/GHSA-44fq-8xcf-3m3h] Lấy cùng một token với bước xác thực tại lối vào và kiểm tra chữ ký, ngăn giả mạo JWT trong header admin-----------
            String token = TokenUtils.getTokenByRequest(req);
            if (StringUtils.isBlank(token)) {
                log.info("[Công cụ AI] Yêu cầu thiếu token, xử lý theo dạng ẩn danh (không có quyền công cụ nghiệp vụ)");
                return Collections.emptySet();
            }
            // Kiểm tra chữ ký, thời hạn hiệu lực và phiên redis; thất bại thì ném ngoại lệ, đi xuống catch bên dưới để xử lý theo dạng ẩn danh
            TokenUtils.verifyToken(token, commonApi, redisUtil);
            //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-9qp4-7428-wvm7/GHSA-44fq-8xcf-3m3h] Lấy cùng một token với bước xác thực tại lối vào và kiểm tra chữ ký, ngăn giả mạo JWT trong header admin-----------
            String username = JwtUtil.getUsername(token);
            if (StringUtils.isBlank(username)) {
                log.info("【Công cụ AI】X-Access-Token Không phân tích được username, xử lý theo dạng ẩn danh (không có quyền công cụ nghiệp vụ)");
                return Collections.emptySet();
            }
            SysUser user = sysUserService.getUserByName(username);
            if (user == null) {
                log.warn("[Công cụ AI] Người dùng trong token không tồn tại trong hệ thống: username={}，xử lý dưới dạng ẩn danh", username);
                return Collections.emptySet();
            }
            Set<String> perms = sysUserService.getUserPermissionsSet(user.getId());
            if (perms == null || perms.isEmpty()) {
                log.info("[Công cụ AI] Người dùng không có bất kỳ mã quyền nào: username={}, userId={}", username, user.getId());
                return Collections.emptySet();
            }
            log.info("Đã phân tích xong quyền hạn của người dùng hiện tại: username={}, perms.size={}", username, perms.size());
            return perms;
        } catch (Exception e) {
            log.warn("Phân tích quyền hạn của người dùng hiện tại thất bại: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Thêm người dùng
     * @return
     * @author chenrui
     * @date 2025/8/27 09:51
     */
    private JeecgLlmTools addUserTool(Set<String> perms){
        log.info("--------【Công cụ AI】Công cụ thêm người dùng------------------");
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("add_user")
                .description("Thêm người dùng,Trả về kết quả thêm;" +
                        "\n\n - Khi thiếu trường bắt buộc,Vui lòng hỏi người dùng." +
                        "\n\n - Bạn nên phán đoán trước xem dữ liệu người dùng nhập vào có hợp lệ hay không,ví dụ tên người dùng có đúng quy chuẩn hay không,số điện thoại và email có đúng không, v.v.." +
                        "\n\n - Dùng tên người dùng truy vấn trước xem người dùng có tồn tại không,Nếu đã tồn tại thì không thể thêm." +
                        "\n\n - Sau khi thêm thành công thì trả về thông báo thành công,Nếu thất bại thì trả về lý do thất bại." +
                        "\n\n - Tên người dùng,Số điện thoại đều yêu cầu duy nhất,Xác nhận tính duy nhất trước bằng công cụ truy vấn người dùng." )
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("username", "Tên người dùng,Bắt buộc,Chỉ được dùng chữ cái, chữ số, dấu gạch dưới và phải bắt đầu bằng chữ cái,Duy nhất")
                                .addStringProperty("password", "Mật khẩu người dùng,Bắt buộc")
                                .addStringProperty("realname", "Họ tên thật,Bắt buộc")
                                //.addStringProperty("email", "Email,Bắt buộc,Duy nhất")
                                .addStringProperty("phone", "Số điện thoại,Bắt buộc,Duy nhất")
                                .required("username","password","realname","phone")
                                .build()
                )
                .build();

        // Xác thực quyền đổi sang dùng tập hợp quyền hạn do bên gọi truyền vào (từ JWT→userId→sysUserService.getUserPermissionsSet）
        final boolean hasAddPermission = perms != null && perms.contains("system:user:add");

        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            // Kiểm tra quyền (dùng kết quả đã bắt trước, tránh gọi Shiro trong luồng bất đồng bộ)
            if (!hasAddPermission) {
                log.warn("【Công cụ AI】add_user Lời gọi bị từ chối: Người dùng hiện tại không có system:user:add Quyền hạn");
                return "Không có quyền: Bạn không có quyền thêm người dùng (system:user:add）";
            }
            JSONObject arguments = JSONObject.parseObject(toolExecutionRequest.arguments());
            arguments.put("confirmPassword",arguments.get("password"));
            arguments.put("userIdentity",1);
            arguments.put("activitiSync",1);
            arguments.put("departIds","");
            String selectedRoles = arguments.getString("selectedroles");
            String selectedDeparts = arguments.getString("selecteddeparts");
            String msg = "Thêm người dùng thất bại";
            try {
                SysUser user = JSON.parseObject(arguments.toJSONString(), SysUser.class);
                user.setCreateTime(new Date());//Đặt thời gian tạo
                String salt = oConvertUtils.randomGen(8);
                user.setSalt(salt);
                String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
                user.setPassword(passwordEncode);
                user.setStatus(1);
                user.setDelFlag(CommonConstant.DEL_FLAG_0);
                //Trường bảng người dùng org_code Không thể đặt giá trị của nó tại đây
                user.setOrgCode(null);
                // Lưu người dùng qua một service để đảm bảo transaction
                //Lấy tenant ids
                String relTenantIds = arguments.getString("relTenantIds");
                sysUserService.saveUser(user, selectedRoles, selectedDeparts, relTenantIds, false);
                baseCommonService.addLog("Thêm người dùng, username: " +user.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
                msg = "Thêm người dùng thành công";
                // Người dùng thay đổi, kích hoạt workflow đồng bộ
            } catch (Exception e) {
                e.printStackTrace();
                msg = "Thêm người dùng thất bại";
            }
            return msg;
        };
        return new JeecgLlmTools(toolSpecification,toolExecutor);
    }

    /**
     * Truy vấn thông tin người dùng
     *
     * @return Chuỗi JSON danh sách người dùng
     * @author chenrui
     * @date 2025/8/26 18:52
     */
    private JeecgLlmTools queryUserTool(Set<String> perms) {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("query_user_by_name")
                .description("Truy vấn thông tin chi tiết của người dùng, trả về mảng JSON. Hỗ trợ truy vấn kết hợp nhiều trường gồm tên người dùng, họ tên thật, email, số điện thoại; các trường tên người dùng, họ tên thật, email, số điện thoại đều là truy vấn mờ. Không có điều kiện thì trả về toàn bộ người dùng.")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("username", "Tên người dùng")
                                .addStringProperty("realname", "Họ tên thật")
                                .addStringProperty("email", "Email")
                                .addStringProperty("phone", "Số điện thoại")
                                .build()
                )
                .build();

        // Xác thực chuyển sang dùng tập hợp quyền hạn do bên gọi truyền vào
        final boolean hasListPermission = perms != null && perms.contains("system:user:listAll");

        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            // Kiểm tra quyền (dùng kết quả đã bắt trước, tránh gọi Shiro trong luồng bất đồng bộ)
            if (!hasListPermission) {
                log.warn("【Công cụ AI】query_user_by_name Lời gọi bị từ chối: Người dùng hiện tại không có system:user:listAll Quyền hạn");
                return "Không có quyền hạn: bạn không có quyền truy vấn danh sách người dùng";
            }
            SysUser args = JSONObject.parseObject(toolExecutionRequest.arguments(), SysUser.class);
            QueryWrapper<SysUser> qw = new QueryWrapper<>();
            if (StringUtils.isNotBlank(args.getUsername())) {
                qw.like("username", args.getUsername());
            }
            if (StringUtils.isNotBlank(args.getRealname())) {
                qw.like("realname", args.getRealname());
            }
            if (StringUtils.isNotBlank(args.getEmail())) {
                qw.like("email", args.getEmail());
            }
            if (StringUtils.isNotBlank(args.getPhone())) {
                qw.like("phone", args.getPhone());
            }
            if (StringUtils.isNotBlank(args.getWorkNo())) {
                qw.eq("work_no", args.getWorkNo());
            }
            qw.eq("del_flag", 0);
            List<SysUser> users = userMapper.selectList(qw);
            users.forEach(u -> { u.setPassword(null); u.setSalt(null); });
            return JSONObject.toJSONString(users);
        };
        return new JeecgLlmTools(toolSpecification, toolExecutor);
    }

    /**
     * Truy vấn tất cả vai trò
     * @return
     * @author chenrui
     * @date 2025/8/27 09:52
     */
    private JeecgLlmTools queryAllRolesTool(Set<String> perms) {
        ToolSpecification spec = ToolSpecification.builder()
                .name("query_all_roles")
                .description("Truy vấn tất cả vai trò, trả về mảng JSON. Bao gồm các trường: id, roleName, roleCode; mặc định sắp xếp theo thời gian tạo/Quy tắc số thứ tự do backend quyết định.")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("roleName", "Tên vai trò")
                                .addStringProperty("roleCode", "Mã vai trò")
                                .build()
                )
                .build();

        // Xác thực chuyển sang dùng tập hợp quyền hạn do bên gọi truyền vào
        final boolean hasRoleListPermission = perms != null && perms.contains("system:role:list");

        ToolExecutor exec = (toolExecutionRequest, memoryId) -> {
            // Kiểm tra quyền (dùng kết quả đã bắt trước, tránh gọi Shiro trong luồng bất đồng bộ)
            if (!hasRoleListPermission) {
                log.warn("【Công cụ AI】query_all_roles Lời gọi bị từ chối: Người dùng hiện tại không có system:role:list Quyền hạn");
                return "Không có quyền: Bạn không có quyền truy vấn danh sách vai trò";
            }
            // Thực hiện truy vấn cách ly tenant (nếu đã bật)
            SysRole sysRole = JSONObject.parseObject(toolExecutionRequest.arguments(), SysRole.class);
            QueryWrapper<SysRole> qw = Wrappers.query();
            if (StringUtils.isNotBlank(sysRole.getRoleName())) {
                qw.like("role_name", sysRole.getRoleName());
            }
            if (StringUtils.isNotBlank(sysRole.getRoleCode())) {
                qw.like("role_code", sysRole.getRoleCode());
            }
            // Chưa xóa
            List<SysRole> roles = sysRoleService.list(qw);
            // Chỉ trả về trường cốt lõi
            JSONArray arr = new JSONArray();
            for (SysRole r : roles) {
                JSONObject o = new JSONObject();
                o.put("id", r.getId());
                o.put("roleName", r.getRoleName());
                o.put("roleCode", r.getRoleCode());
                arr.add(o);
            }
            return arr.toJSONString();
        };
        return new JeecgLlmTools(spec, exec);
    }

    /**
     * Cấp vai trò cho người dùng
     * @return
     * @author chenrui
     * @date 2025/8/27 09:52
     */
    private JeecgLlmTools grantUserRolesTool(Set<String> perms) {
        ToolSpecification spec = ToolSpecification.builder()
                .name("grant_user_roles")
                .description("Cấp vai trò cho người dùng, hỗ trợ cấp nhiều vai trò trong một lần; nếu quan hệ đã tồn tại thì bỏ qua. Trả về thống kê kết quả cấp.")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("userId", "ID người dùng, bắt buộc")
                                .addStringProperty("roleIds", "Danh sách ID vai trò, bắt buộc, dùng dấu phẩy tiếng Anh để phân tách")
                                .required("userId","roleIds")
                                .build()
                )
                .build();

        // Xác thực chuyển sang dùng tập hợp quyền hạn do bên gọi truyền vào
        final boolean hasGrantPermission = perms != null
                && (perms.contains("system:user:addUserRole") || perms.contains("system:user:edit"));

        ToolExecutor exec = (toolExecutionRequest, memoryId) -> {
            // Kiểm tra quyền (dùng kết quả đã bắt trước, tránh gọi Shiro trong luồng bất đồng bộ)
            if (!hasGrantPermission) {
                log.warn("【Công cụ AI】grant_user_roles Lời gọi bị từ chối: Người dùng hiện tại không có system:user:addUserRole hoặc system:user:edit Quyền hạn");
                return "Không có quyền hạn: bạn không có quyền gán vai trò cho người dùng";
            }
            JSONObject args = JSONObject.parseObject(toolExecutionRequest.arguments());
            String userId = args.getString("userId");
            String roleIdsStr = args.getString("roleIds");
            if (StringUtils.isAnyBlank(userId, roleIdsStr)) {
                return "Thiếu tham số: userId hoặc roleIds";
            }
            SysUser user = sysUserService.getById(userId);
            if (user == null) {
                return "Người dùng không tồn tại: " + userId;
            }
            String[] roleIds = roleIdsStr.split(",");
            int added = 0, existed = 0, invalid = 0;
            for (String roleId : roleIds) {
                roleId = roleId.trim();
                if (roleId.isEmpty()) continue;
                SysRole role = sysRoleService.getById(roleId);
                if (role == null) { invalid++; continue; }
                QueryWrapper<org.jeecg.modules.system.entity.SysUserRole> q = new QueryWrapper<>();
                q.eq("role_id", roleId).eq("user_id", userId);
                org.jeecg.modules.system.entity.SysUserRole one = sysUserRoleService.getOne(q);
                if (one == null) {
                    org.jeecg.modules.system.entity.SysUserRole rel = new org.jeecg.modules.system.entity.SysUserRole(userId, roleId);
                    boolean ok = sysUserRoleService.save(rel);
                    if (ok) { added++; } else { invalid++; }
                } else {
                    existed++;
                }
            }
            return String.format("Cấp quyền hoàn tất: Đã thêm mới%d，đã tồn tại%d, không hợp lệ/Thất bại%d", added, existed, invalid);
        };
        return new JeecgLlmTools(spec, exec);
    }
}
