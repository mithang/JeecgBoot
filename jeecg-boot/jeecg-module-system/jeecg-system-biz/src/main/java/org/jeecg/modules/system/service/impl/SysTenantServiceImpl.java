package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.constant.enums.SysAnnmentTypeEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.aop.TenantLog;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.tenant.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Lớp triển khai tenant
 * @author: jeecg-boot
 */
@Service("sysTenantServiceImpl")
@Slf4j
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {

    @Autowired
    ISysUserService userService;
    @Autowired
    private SysUserTenantMapper userTenantMapper;
    @Autowired
    private SysTenantMapper tenantMapper;

    @Autowired
    private ISysTenantPackService sysTenantPackService;

    @Autowired
    private SysTenantPackUserMapper sysTenantPackUserMapper;

    @Autowired
    private ISysBaseAPI sysBaseApi;
    
    @Autowired
    private SysUserDepartMapper sysUserDepartMapper;

    @Autowired
    private SysTenantPackMapper sysTenantPackMapper;
    
    @Autowired
    private SysPackPermissionMapper sysPackPermissionMapper;

    @Override
    public List<SysTenant> queryEffectiveTenant(Collection<Integer> idList) {
        if(oConvertUtils.listIsEmpty(idList)){
            return null;
        }
        
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysTenant::getId, idList);
        queryWrapper.eq(SysTenant::getStatus, Integer.valueOf(CommonConstant.STATUS_1));
        //Truy vấn tại đây bỏ qua điều kiện thời gian
        return super.list(queryWrapper);
    }

    @Override
    public Long countUserLinkTenant(String id) {
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId,id);
        query.eq(SysUserTenant::getStatus,CommonConstant.STATUS_1);
        // Tìm số lượng người dùng đã được liên kết
        return userTenantMapper.selectCount(query);
    }

    @Override
    public boolean removeTenantById(String id) {
        // Tìm số lượng người dùng đã được liên kết
        return super.removeById(Integer.parseInt(id));
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public void invitationUserJoin(String ids, String phone,String username) {
        String[] idArray = ids.split(SymbolConstant.COMMA);
        String userId = null;
        SysUser userByPhone = null;
        // Giải thích logic mã: 【QQYUN-4605] Chức năng mời người khác tham gia tenant trong màn hình quản trị không thể chọn người dùng không thuộc tenant, mời qua số điện thoại------------
        if(oConvertUtils.isNotEmpty(phone)){
            userByPhone = userService.getUserByPhone(phone);
            //Cho thấy người dùng không tồn tại
            if(null == userByPhone){
                throw new JeecgBootException("Người dùng hiện tại không tồn tại, vui lòng kiểm tra số điện thoại");
            }
            userId = userByPhone.getId();
        }else{
            userByPhone = userService.getUserByName(username);
            //Cho thấy người dùng không tồn tại
            if(null == userByPhone){
                throw new JeecgBootException("Người dùng hiện tại không tồn tại, vui lòng kiểm tra số điện thoại");
            }
            userId = userByPhone.getId();
        }

        //Lặp qua tenant id
        for (String id:idArray) {
            //Lấy thông tin người được mời có tồn tại hay không
            SysUserTenant userTenant = userTenantMapper.getUserTenantByTenantId(userId, Integer.valueOf(id));
            if(null == userTenant){
                SysUserTenant relation = new SysUserTenant();
                relation.setUserId(userId);
                relation.setTenantId(Integer.valueOf(id));
                relation.setStatus(CommonConstant.USER_TENANT_NORMAL);
                userTenantMapper.insert(relation);
                //Thêm tất cả gói cước thuộc tenant cho người dùng hiện tại
                this.addPackUser(userId,id);
                //Mời người dùng tham gia tenant, gửi tin nhắn
                this.sendInvitationTenantMessage(userByPhone,id);
            }else{
                // Giải thích logic mã: 【QQYUN-5885】Lời nhắc mời người dùng tham gia không chính xác------------
                String tenantErrorInfo = getTenantErrorInfo(userTenant.getStatus());
                String errMsg = "Người dùng số điện thoại::" + userByPhone.getPhone() + " Biệt danh:" + userByPhone.getRealname() + "，" + tenantErrorInfo;
                throw new JeecgBootException(errMsg);
            }
        }
    }

    /**
     * Gửi tin nhắn mời tham gia tenant trong low-code
     * 
     * @param user
     * @param id
     */
    private void sendInvitationTenantMessage(SysUser user, String id) {
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        // Gửi tin nhắn
        SysTenant sysTenant = this.baseMapper.querySysTenant((Integer.valueOf(id)));
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        messageDTO.setFromUser("system");
        String title = sysUser.getRealname() + " đã mời bạn tham gia "+sysTenant.getName()+"。";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(title);
        messageDTO.setType("system");
        messageDTO.setCategory(CommonConstant.MSG_CATEGORY_1);
        sysBaseApi.sendSysAnnouncement(messageDTO);
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public void leaveTenant(String userIds, String tenantId) {
        String[] userIdArray = userIds.split(SymbolConstant.COMMA);
        for (String userId:userIdArray) {
            // Giải thích logic mã: [QQYUN-3371]Cải tạo logic Tenant, đổi sang bảng quan hệ------------
            LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
            query.eq(SysUserTenant::getTenantId,tenantId);
            query.eq(SysUserTenant::getUserId,userId);
            userTenantMapper.delete(query);
            //Giải thích logic mã: 【QQYUN-13720】Di chuyển người dùng ra khỏi tenant hiện tại, không có nhắc nhở hệ thống---
            // Gửi tin nhắn cho người bị xóa
            SysTenantPackUser sysTenantPackUser = new SysTenantPackUser();
            sysTenantPackUser.setTenantId(Integer.valueOf(tenantId));
            sysTenantPackUser.setUserId(userId);
            sendMsgForDelete(sysTenantPackUser);
        }
        //Tenant gỡ người dùng thì xóa trực tiếp gói sản phẩm tenant của người dùng
        sysTenantPackUserMapper.deletePackUserByTenantId(Integer.valueOf(tenantId),Arrays.asList(userIds.split(SymbolConstant.COMMA)));
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public Integer saveTenantJoinUser(SysTenant sysTenant, String userId) {
        //Thêm tenant
        this.saveTenant(sysTenant);
        
        // Thêm gói sản phẩm tenant
        Integer tenantId = sysTenant.getId();
        sysTenantPackService.addDefaultTenantPack(tenantId);
        
        //Thêm tenant vào bảng quan hệ
        return tenantId;
    }

    @Override
    public void saveTenant(SysTenant sysTenant){
        //Lấy tenant id
        sysTenant.setId(this.tenantIdGenerate());
        sysTenant.setHouseNumber(RandomUtil.randomStringUpper(6));
        sysTenant.setDelFlag(CommonConstant.DEL_FLAG_0);
        this.save(sysTenant);
        //Giải thích logic mã:【QQYUN-5723】1. Thêm người tạo hiện tại vào quan hệ tenant------------
        //của người hiện đang đăng nhập id
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        this.saveTenantRelation(sysTenant.getId(),loginUser.getId());
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public Integer joinTenantByHouseNumber(SysTenant sysTenant, String userId) {
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysTenant::getHouseNumber,sysTenant.getHouseNumber());
        SysTenant one = this.getOne(query);
        //Cần trả về tenant id(Dùng để frontend cập nhật bộ nhớ đệm),Trả về 0 nghĩa là số nhà (mã tenant) hiện tại không tồn tại
        if(null == one){
            return 0;
        }else{
            LambdaQueryWrapper<SysUserTenant> relationQuery = new LambdaQueryWrapper<>();
            relationQuery.eq(SysUserTenant::getTenantId,one.getId());
            relationQuery.eq(SysUserTenant::getUserId,userId);
            SysUserTenant relation = userTenantMapper.selectOne(relationQuery);
            if(relation != null){
                String msg = "";
                if(CommonConstant.USER_TENANT_UNDER_REVIEW.equals(relation.getStatus())){
                    msg = ",Trạng thái:Đang xét duyệt";
                }else if(CommonConstant.USER_TENANT_REFUSE.equals(relation.getStatus())){
                    throw new JeecgBootBizTipException("Quản trị viên đã từ chối bạn tham gia Tenant,Vui lòng liên hệ quản trị viên tenant");
                }else if(CommonConstant.USER_TENANT_QUIT.equals(relation.getStatus())){
                    msg = ",Trạng thái:Đã nghỉ việc";
                }
                throw new JeecgBootBizTipException("Bạn đã là thành viên của tenant này"+msg);
            }
            //Trạng thái người dùng đang chờ xét duyệt khi gia nhập bằng mã số nhà
            SysUserTenant tenant = new SysUserTenant();
            tenant.setTenantId(one.getId());
            tenant.setUserId(userId);
            tenant.setStatus(CommonConstant.USER_TENANT_UNDER_REVIEW);
            userTenantMapper.insert(tenant);

            // QQYUN-4526【Ứng dụng】Thông báo tham gia tổ chức
            sendMsgForApplyJoinTenant(userId, one);
            return tenant.getTenantId();
        }
    }

    @Override
    public Integer countCreateTenantNum(String userId) {
        return this.userTenantMapper.countCreateTenantNum(userId);
    }

    @Override
    public IPage<SysTenant> getRecycleBinPageList(Page<SysTenant> page, SysTenant sysTenant) {
        return page.setRecords(tenantMapper.getRecycleBinPageList(page,sysTenant));
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public void deleteTenantLogic(String ids) {
        String[] idArray = ids.split(SymbolConstant.COMMA);
        List<Integer> list = new ArrayList<>();
        //Chuyển thành kiểu int
        for (String id:idArray) {
            list.add(Integer.valueOf(id));
        }
        //Xóa tenant
        tenantMapper.deleteByTenantId(list);
        //Xóa người dùng thuộc tenant
        userTenantMapper.deleteUserByTenantId(list);

        //Xóa gói sản phẩm thuộc Tenant
        this.deleteTenantPackByTenantId(list);
    }

    @Override
    public void revertTenantLogic(String ids) {
        String[] idArray = ids.split(SymbolConstant.COMMA);
        List<Integer> list = new ArrayList<>();
        //Chuyển thành kiểu int
        for (String id:idArray) {
            list.add(Integer.valueOf(id));
        }
        //Khôi phục tenant
        tenantMapper.revertTenantLogic(list);
    }

    /**
     * Thêm tenant vào bảng quan hệ
     * @param tenantId
     * @param userId
     */
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public Integer saveTenantRelation(Integer tenantId,String userId) {
        SysUserTenant relation = new SysUserTenant();
        relation.setTenantId(tenantId);
        relation.setUserId(userId);
        relation.setStatus(CommonConstant.USER_TENANT_NORMAL);
        userTenantMapper.insert(relation);
        return relation.getTenantId();
    }

    /**
     * Lấy tenant id
     * @return
     */
   public int tenantIdGenerate(){
       synchronized (this){
           //Lấy giá trị lớn nhất id
           // Giải thích logic mã: Khi cơ sở dữ liệu không có thuê bao, nếu để trống sẽ báo lỗi kiểu trả về của SQL không khớp------------
           int maxTenantId = oConvertUtils.getInt(tenantMapper.getMaxTenantId(),0);
           if(maxTenantId >= 1000){
               return maxTenantId + 1;
           }else{
               return 1000;
           }
       }
   }


    @Override
    public void exitUserTenant(String userId, String username, String tenantId) {
        int tId = Integer.parseInt(tenantId);
        //Lấy tất cả thông tin tenant
        List<String> userIdsByTenantId = userTenantMapper.getUserIdsByTenantId(tId);
        //Truy vấn Tenant hiện tại có phải là chủ sở hữu hay không
        SysTenant sysTenant = tenantMapper.selectById(tId);
        //Nếu là chủ sở hữu
        if (username.equals(sysTenant.getCreateBy())) {
            //Xác định số chữ số của thông tin Tenant hiện tại
            if (null != userIdsByTenantId && userIdsByTenantId.size() > 1) {
                //Cần chỉ định chủ sở hữu
                throw new JeecgBootException("assignedOwen");
            } else if (null != userIdsByTenantId && userIdsByTenantId.size() == 1) {
                //Chỉ khi là chủ sở hữu mới cần hủy tenant
                throw new JeecgBootException("cancelTenant");
            } else {
                throw new JeecgBootException("Rời khỏi tenant thất bại, thông tin tenant đã không còn tồn tại");
            }
        } else {
            //Không phải chủ sở hữu thì xóa trực tiếp
            this.leaveTenant(userId, tenantId);
            this.leveUserProcess(userId, tenantId);
        }
    }

    @Override
    public void changeOwenUserTenant(String userId, String tId) {
        //Truy vấn người dùng hiện tại có tồn tại trong tenant này không
        // Giải thích logic mã: Id tenant phải là id được truyền qua, không phải là tenant hiện tại------------
        int tenantId = oConvertUtils.getInt(tId, 0);
        SysTenant sysTenant = tenantMapper.selectById(tenantId);
        if(null == sysTenant){
            throw new JeecgBootException("Rời khỏi tenant thất bại, tenant này không tồn tại");
        }
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-8v96-whwg-rf92/GHSA-xpvg-j72w-356p/GHSA-3f75-3wg8-c663] Chỉ chủ sở hữu tenant hoặc admin mới được đổi chủ sở hữu, ngăn thành viên tùy ý tiếp quản tenant-----------
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (!loginUser.getUsername().equals(sysTenant.getCreateBy()) && !SecurityUtils.getSubject().hasRole("admin")) {
            throw new JeecgBootException("Chỉ chủ sở hữu tenant mới có thể thay đổi chủ sở hữu");
        }
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-8v96-whwg-rf92/GHSA-xpvg-j72w-356p/GHSA-3f75-3wg8-c663] Chỉ chủ sở hữu tenant hoặc admin mới được đổi chủ sở hữu, ngăn thành viên tùy ý tiếp quản tenant-----------
        String createBy = sysTenant.getCreateBy();
        Integer count = userTenantMapper.userTenantIzExist(userId, tenantId);
        if (count == 0) {
            throw new JeecgBootException("Rời khỏi tenant thất bại, tenant này không có người dùng đó");
        }
        //Lấy thông tin người dùng
        SysUser user = userService.getById(userId);
        //Thay đổi chủ sở hữu
        SysTenant tenant = new SysTenant();
        tenant.setCreateBy(user.getUsername());
        tenant.setId(tenantId);
        tenantMapper.updateById(tenant);
        //Xóa thông tin tenant của người dùng đang đăng nhập
        //update-begin---author:wangshuai ---date:20230705  for: Sau khi chủ sở hữu cũ rời đi, cần đổi quan hệ người dùng - thuê bao của chủ sở hữu cũ thành đã nghỉ việc------------
        //Lấy id người dùng của người tạo ban đầu id
        SysUser userByName = userService.getUserByName(createBy);
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId,userByName.getId());
        query.eq(SysUserTenant::getTenantId,tenantId);
        SysUserTenant userTenant = new SysUserTenant();
        userTenant.setStatus(CommonConstant.USER_TENANT_QUIT);
        userTenantMapper.update(userTenant,query);
        //Quy trình nghỉ việc
        this.leveUserProcess(userId, String.valueOf(tenantId));
    }

    /**
     * Kích hoạt quy trình nghỉ việc
     *
     * @param userId
     * @param tenantId
     * @param tenantId
     */
    private void leveUserProcess(String userId, String tenantId) {
        LoginUser userInfo = new LoginUser();
        SysUser user = userService.getById(userId);
    }

    @Override
    public Result<String> invitationUser(String phone, String departId) {
        Result<String> result = new Result<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //1, truy vấn thông tin người dùng,Kiểm tra xem người dùng có tồn tại không
        SysUser userByPhone = userService.getUserByPhone(phone);
        if(null == userByPhone){
            result.setSuccess(false);
            result.setMessage("Người dùng không tồn tại");
            return result;
        }
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);

        //2.Phán đoán người mời hiện tại có nằm trong danh sách tenant hay không
        Integer userCount = userTenantMapper.userTenantIzExist(sysUser.getId(), tenantId);
        if(userCount == 0){
            result.setSuccess(false);
            result.setMessage("Quản trị viên hiện tại không có quyền mời");
            return result;
        }

        //3.Chèn vào thông tin tenant, những mục đã tồn tại thì không được chèn
        //Lấy thông tin người được mời có tồn tại hay không
        SysUserTenant sysUserTenant = userTenantMapper.getUserTenantByTenantId(userByPhone.getId(), tenantId);
        //Người dùng đã tồn tại
        if(null != sysUserTenant){
            result.setSuccess(false);
            String tenantErrorInfo = getTenantErrorInfo(sysUserTenant.getStatus());
            String msg = "Người dùng số điện thoại::" + userByPhone.getPhone() + " Biệt danh:" + userByPhone.getRealname() + "，" + tenantErrorInfo;
            result.setMessage(msg);
            return result;
        }

        //4.Cần người dùng đồng ý thủ công để tham gia
        String status = CommonConstant.USER_TENANT_INVITE;

        //5.Người dùng hiện tại không tồn tại trong Tenant,thì cần thêm người dùng vào Tenant
        SysUserTenant tenant = new SysUserTenant();
        tenant.setTenantId(tenantId);
        tenant.setUserId(userByPhone.getId());
        tenant.setStatus(status);
        userTenantMapper.insert(tenant);
        result.setSuccess(true);
        result.setMessage("Mời thành viên thành công, thành viên đồng ý mới có thể tham gia");

        //6.Lưu quan hệ phòng ban người dùng
        if(oConvertUtils.isNotEmpty(departId)){
            //Lưu quan hệ phòng ban người dùng
            this.saveUserDepart(userByPhone.getId(),departId);
        }
        
        //  QQYUN-4527【Ứng dụng】Mời thành viên tham gia tổ chức, gửi nhắc nhở bằng tin nhắn
        sendMsgForInvitation(userByPhone, tenantId, sysUser.getRealname());
        return result;
    }

    @Override
    public TenantDepartAuthInfo getTenantDepartAuthInfo(Integer tenantId) {
        SysTenant sysTenant = this.getById(tenantId);
        if(sysTenant==null) {
            return null;
        }

        TenantDepartAuthInfo info = new TenantDepartAuthInfo();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        boolean superAdmin = false;
        // Truy vấn bảng pack
        List<String> packCodeList = baseMapper.queryUserPackCode(tenantId, userId);
        if(packCodeList==null || packCodeList.size()==0){
            //Nếu không có dữ liệu, phán đoán xem người tạo tenant có phải là người dùng hiện tại hay không
            if(sysTenant.getCreateBy().equals(sysUser.getUsername())){
                sysTenantPackService.addDefaultTenantPack(tenantId);
                superAdmin = true;
            }else{
                superAdmin = false;
            }
        }
        if(superAdmin == false){
            List<TenantPackUserCount> packCountList = baseMapper.queryTenantPackUserCount(tenantId);
            info.setPackCountList(packCountList);
        }
        info.setSysTenant(sysTenant);
        info.setSuperAdmin(superAdmin);
        info.setPackCodes(packCodeList);
        return info;
    }

    @Override
    public List<TenantPackUserCount> queryTenantPackUserCount(Integer tenantId) {
        return baseMapper.queryTenantPackUserCount(tenantId);
    }

    @Override
    public TenantPackModel queryTenantPack(TenantPackModel model) {
        Integer tenantId = model.getTenantId();
        String packCode = model.getPackCode();

        SysTenantPack sysTenantPack = sysTenantPackService.getSysTenantPack(tenantId, packCode);
        if(sysTenantPack!=null){
            TenantPackModel tenantPackModel = new TenantPackModel();
            tenantPackModel.setPackName(sysTenantPack.getPackName());
            tenantPackModel.setPackId(sysTenantPack.getId());
            // Truy vấn người dùng
            List<TenantPackUser> userList = getTenantPackUserList(tenantId, sysTenantPack.getId(), 1);
            tenantPackModel.setUserList(userList);
            return tenantPackModel;
        }
        return null;
    }

    @Override
    public void addBatchTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        String userIds = sysTenantPackUser.getUserId();
        if(oConvertUtils.isNotEmpty(userIds)){
            ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
            String realNames = sysTenantPackUser.getRealname();
            String[] userIdArray = userIds.split(",");
            String[] realNameArray = realNames.split(",");
            for(int i=0;i<userIdArray.length;i++){
                String userId = userIdArray[i];
                String realName = realNameArray[i];
                SysTenantPackUser entity = new SysTenantPackUser(sysTenantPackUser, userId, realName);
                LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                        .eq(SysTenantPackUser::getTenantId, entity.getTenantId())
                        .eq(SysTenantPackUser::getPackId, entity.getPackId())
                        .eq(SysTenantPackUser::getUserId, entity.getUserId());
                SysTenantPackUser packUser = sysTenantPackUserMapper.selectOne(query);
                if(packUser==null || packUser.getId()==null){
                    currentService.addTenantPackUser(entity);
                }else{
                    if(packUser.getStatus()==0){
                        packUser.setPackName(entity.getPackName());
                        packUser.setRealname(realName);
                        currentService.addTenantPackUser(packUser);
                    }
                }
            }
        }
    }

    @TenantLog(2)
    @Override
    public void addTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        if(sysTenantPackUser.getId()==null){
            sysTenantPackUserMapper.insert(sysTenantPackUser);
        }else{
            sysTenantPackUser.setStatus(1);
            sysTenantPackUserMapper.updateById(sysTenantPackUser);
        }
    }

    @TenantLog(4)
    @Override
    public void deleteTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId())
                .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId());
        sysTenantPackUserMapper.delete(query);

        // QQYUN-4525【Quản lý tổ chức】Quản trị viên 2.Khi quyền quản trị viên bị gỡ, gửi tin nhắn cho người bị gỡ
        sendMsgForDelete(sysTenantPackUser);
    }

    @Override
    public List<TenantPackUser> getTenantPackApplyUsers(Integer tenantId) {
        return getTenantPackUserList(tenantId, null, 0);
    }

    /**
     * Lấy người dùng của một gói sản phẩm thuộc tenant
     * Hoặc là người dùng đăng ký gói sản phẩm thuộc tenant
     * @param tenantId
     * @param packId
     * @param packUserStatus
     * @return
     */
    private List<TenantPackUser> getTenantPackUserList(Integer tenantId, String packId, Integer packUserStatus){
        // Truy vấn người dùng
        List<TenantPackUser> userList = baseMapper.queryPackUserList(tenantId, packId, packUserStatus);
        if(userList!=null && userList.size()>0){
            List<String> userIdList = userList.stream().map(i->i.getId()).collect(Collectors.toList());
            // Phòng ban
            List<UserDepart> depList = baseMapper.queryUserDepartList(userIdList);
            // Chức vụ TODO
            // Duyệt người dùng, thêm thông tin phòng ban và thông tin vị trí vào người dùng
            for(TenantPackUser user: userList){
                for(UserDepart dep: depList){
                    if(user.getId().equals(dep.getUserId())){
                        user.addDepart(dep.getDepartName());
                    }
                }
            }
        }
        return userList;
    }

    @Override
    public void doApplyTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPack> query1 = new LambdaQueryWrapper<SysTenantPack>()
                .eq(SysTenantPack::getTenantId, sysTenantPackUser.getTenantId())
                .eq(SysTenantPack::getPackCode, sysTenantPackUser.getPackCode());
        SysTenantPack pack = sysTenantPackService.getOne(query1);
        if(pack!=null){
            sysTenantPackUser.setStatus(0);
            sysTenantPackUser.setPackId(pack.getId());
            LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                    .eq(SysTenantPackUser::getTenantId, sysTenantPackUser.getTenantId())
                    .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId())
                    .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId());
            Long count = sysTenantPackUserMapper.selectCount(query);
            if(count==null || count==0){
                sysTenantPackUserMapper.insert(sysTenantPackUser);
            }
            // QQYUN-4524【Liên kết tổ chức】Quản trị viên 1.Đơn xin cấp quyền quản trị viên-> Gửi tin nhắn thông báo cho quản trị viên liên quan
            sendMsgForApply(sysTenantPackUser.getUserId(), pack);
        }
    }

    /**
     * Đăng ký quyền quản trị viên — Gửi tin nhắn
     * @param userId
     * @param pack
     */
    private void sendMsgForApply(String userId, SysTenantPack pack){
        // Gửi tin nhắn
        SysUser user = userService.getById(userId);
        Integer tenantId = pack.getTenantId();
        SysTenant sysTenant = this.baseMapper.querySysTenant(tenantId);
        String packCode = pack.getPackCode();

        List<String> packCodeList = Arrays.asList(packCode.split(","));
        List<String> userList = sysTenantPackUserMapper.queryTenantPackUserNameList(tenantId, packCodeList);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(String.join(",", userList));
        messageDTO.setFromUser("system");
        String title = user.getRealname()+" Xin gia nhập "+sysTenant.getName()+" của "+pack.getPackName()+" thành viên.";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(title);
        messageDTO.setType("system");
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     * Thu hồi quyền quản trị viên — Gửi tin nhắn
     * @param sysTenantPackUser
     */
    private void sendMsgForDelete(SysTenantPackUser sysTenantPackUser){
        // Gửi tin nhắn
        SysUser user = userService.getById(sysTenantPackUser.getUserId());
        SysTenant sysTenant = this.baseMapper.querySysTenant(sysTenantPackUser.getTenantId());
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        //Ở low-code, việc chuyển người dùng ra đã có nhắc nhở; khi chuyển người dùng ra không có khái niệm tên gói dịch vụ
        String title = "";
        if(oConvertUtils.isNotEmpty(sysTenantPackUser.getPackName())){
            title = "Bạn đã bị  "+loginUser.getRealname()+" "+sysTenant.getName()+" của "+sysTenantPackUser.getPackName()+"bị xóa khỏi.";
        } else {
            title = "Bạn đã bị  "+loginUser.getRealname()+" "+sysTenant.getName() + "bị xóa khỏi.";
        }
        messageDTO.setTitle(title);
        messageDTO.setFromUser("system");
        Map<String, Object> data = new HashMap<>();
        data.put("realname", loginUser.getRealname());
        data.put("tenantName", sysTenant.getName());
        data.put("packName", sysTenantPackUser.getPackName());
        messageDTO.setData(data);
        messageDTO.setType("system");
        messageDTO.setContent(title);
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     * Đơn đăng ký tham gia tổ chức — Gửi tin nhắn
     * @param userId
     * @param sysTenant
     */
    private void sendMsgForApplyJoinTenant(String userId, SysTenant sysTenant){
        // Gửi tin nhắn
        SysUser user = userService.getById(userId);
        // Gửi tin nhắn cho quản trị viên cấp cao và quản trị viên tổ chức
        String codes = "superAdmin,accountAdmin";
        List<String> packCodeList = Arrays.asList(codes.split(","));
        List<String> userList = sysTenantPackUserMapper.queryTenantPackUserNameList(sysTenant.getId(), packCodeList);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(String.join(",", userList));
        messageDTO.setFromUser("system");
        String title = user.getRealname()+" Xin gia nhập "+sysTenant.getName()+"。";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setType("system");
        messageDTO.setContent(title);
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     *  Mời thành viên, gửi tin nhắn
     * @param user
     * @param tenantId
     * @param realname
     */
    private void sendMsgForInvitation(SysUser user, Integer tenantId, String realname){
        // Gửi tin nhắn
        SysTenant sysTenant = this.baseMapper.querySysTenant(tenantId);
        BusMessageDTO messageDTO = new BusMessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        messageDTO.setFromUser("system");
        // Giải thích logic mã: 【QQYUN-5730] Thông báo mời tham gia tenant nên hiển thị tên người mời------------
        String title = realname + " mời bạn tham gia "+sysTenant.getName()+"。";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(title);
        messageDTO.setType("system");
        // Giải thích logic mã: 【QQYUN-7168] Khi mời thành viên sẽ báo lỗi, nhưng thực tế đã mời thành công---
        messageDTO.setCategory(CommonConstant.MSG_CATEGORY_1);
        // Giải thích logic mã: 【QQYUN-5726】Mời tham gia tenant, thêm nút để nhảy trực tiếp tới đó------------
        messageDTO.setBusType(SysAnnmentTypeEnum.TENANT_INVITE.getType());
        sysBaseApi.sendBusAnnouncement(messageDTO);
    }


    @Override
    public void passApply(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                .eq(SysTenantPackUser::getTenantId, sysTenantPackUser.getTenantId())
                .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId())
                .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId());
        SysTenantPackUser packUser = sysTenantPackUserMapper.selectOne(query);
        if(packUser!=null && packUser.getId()!=null && packUser.getStatus()==0){
            ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
            packUser.setPackName(sysTenantPackUser.getPackName());
            packUser.setRealname(sysTenantPackUser.getRealname());
            currentService.addTenantPackUser(packUser);
            
            //Quản trị viên cấp cao tham gia thành công, gửi tin nhắn hệ thống
            SysTenant sysTenant = tenantMapper.selectById(sysTenantPackUser.getTenantId());
            String content = "  Bạn đã tham gia thành công "+sysTenant.getName()+"thành viên của quản trị viên cấp cao.";
            SysUser sysUser = userService.getById(sysTenantPackUser.getUserId());
            this.sendMsgForAgreeAndRefuseJoin(sysUser,content);
        }
    }

    @Override
    public void deleteApply(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                .eq(SysTenantPackUser::getTenantId, sysTenantPackUser.getTenantId())
                .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId())
                .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId());
        SysTenantPackUser packUser = sysTenantPackUserMapper.selectOne(query);
        if(packUser!=null && packUser.getId()!=null && packUser.getStatus()==0){
            sysTenantPackUserMapper.deleteById(packUser.getId());
            //Quản trị viên cấp cao từ chối tham gia, gửi tin nhắn hệ thống
            SysTenant sysTenant = tenantMapper.selectById(sysTenantPackUser.getTenantId());
            String content = " Quản trị viên đã từ chối yêu cầu tham gia của bạn"+sysTenant.getName()+" với tư cách thành viên quản trị viên cấp cao.";
            SysUser sysUser = userService.getById(sysTenantPackUser.getUserId());
            this.sendMsgForAgreeAndRefuseJoin(sysUser,content);
        }
    }

    @Override
    public IPage<TenantPackUser> queryTenantPackUserList(String tenantId, String packId,Integer status, Page<TenantPackUser> page) {
        // Truy vấn người dùng
        List<TenantPackUser> userList = baseMapper.queryTenantPackUserList(page,tenantId, packId,status);
        // Lấy phòng ban và chức vụ của người dùng thuộc gói sản phẩm
        userList = getPackUserPositionAndDepart(userList);
        return page.setRecords(userList);
    }

    /**
     * Lấy chức vụ và phòng ban của người dùng
     * @param userList
     * @return
     */
    private List<TenantPackUser> getPackUserPositionAndDepart(List<TenantPackUser> userList) {
        if(userList!=null && userList.size()>0){
            List<String> userIdList = userList.stream().map(i->i.getId()).collect(Collectors.toList());
            // Phòng ban
            List<UserDepart> depList = baseMapper.queryUserDepartList(userIdList);
            // Chức vụ
            List<UserPosition> userPositions = baseMapper.queryUserPositionList(userIdList);
            // Duyệt người dùng, thêm thông tin phòng ban và thông tin vị trí vào người dùng
            for (TenantPackUser user : userList) {
                //Thêm phòng ban
                for (UserDepart dep : depList) {
                    if (user.getId().equals(dep.getUserId())) {
                        user.addDepart(dep.getDepartName());
                    }
                }
                //Thêm chức vụ
                for (UserPosition userPosition : userPositions) {
                    if (user.getId().equals(userPosition.getUserId())) {
                        user.addPosition(userPosition.getPositionName());
                    }
                }
            }
        }
        return userList;
    }


    /**
     * Lưu quan hệ phòng ban người dùng
     * @param userId
     * @param departId
     */
    private void saveUserDepart(String userId, String departId) {
        //Lấy số lượng theo id người dùng và id phòng ban,Dùng để xem người dùng có tồn tại trong bảng quan hệ người dùng - phòng ban hay không
        Long count = sysUserDepartMapper.getCountByDepartIdAndUserId(userId,departId);
        if(count == 0){
            SysUserDepart sysUserDepart = new SysUserDepart(userId,departId);
            sysUserDepartMapper.insert(sysUserDepart);
        }
    }
    
    @Override
    public Long getApplySuperAdminCount() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        return baseMapper.getApplySuperAdminCount(sysUser.getId(),tenantId);
    }

    /**
     * Đồng ý hoặc từ chối tham gia, gửi tin nhắn cho siêu quản trị viên
     * @param user
     * @param content
     */
    public void sendMsgForAgreeAndRefuseJoin(SysUser user, String content){
        // Gửi tin nhắn
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        messageDTO.setFromUser("system");
        messageDTO.setTitle(content);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(content);
        messageDTO.setType("system");
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     * Lấy thông tin gợi ý lỗi của Tenant
     *
     * @param status
     * @return
     */
    private String getTenantErrorInfo(String status) {
        String content = "Đã ở trong Tenant, không cần mời!";
        if (CommonConstant.USER_TENANT_QUIT.equals(status)) {
            content = "Đã nghỉ việc!";
        } else if (CommonConstant.USER_TENANT_UNDER_REVIEW.equals(status)) {
            content = "Quản trị viên Tenant đang xét duyệt!";
        } else if (CommonConstant.USER_TENANT_REFUSE.equals(status)) {
            content = "Quản trị viên Tenant đã từ chối!";
        } else if (CommonConstant.USER_TENANT_INVITE.equals(status)) {
            content = "Đã được mời, đang chờ người dùng đồng ý!";
        }
        return content;
    }

    /**
     * Xóa gói sản phẩm thuộc Tenant
     *
     * @param tenantIdList
     */
    private void deleteTenantPackByTenantId(List<Integer> tenantIdList) {
        //1.Xóa người dùng thuộc gói sản phẩm
        sysTenantPackUserMapper.deletePackUserByTenantIds(tenantIdList);
        //2.Xóa quyền hạn menu tương ứng với gói sản phẩm
        sysPackPermissionMapper.deletePackPermByTenantIds(tenantIdList);
        //3.Xóa gói sản phẩm
        sysTenantPackMapper.deletePackByTenantIds(tenantIdList);
    }

    @Override
    public void deleteUserByPassword(SysUser sysUser, Integer tenantId) {
        //Người dùng của người bị xóa id
        String userId = sysUser.getId();
        //Mật khẩu của người bị xóa
        String password = sysUser.getPassword();
        //Người dùng đang đăng nhập
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //step1 Phán đoán người dùng hiện tại có phải quản trị viên của tenant hiện tại hay không(Chỉ siêu quản trị viên và quản trị viên tài khoản mới được xóa)
        Long isHaveAdmin = sysTenantPackUserMapper.izHaveBuyAuth(user.getId(), tenantId);
        if(null == isHaveAdmin || 0 == isHaveAdmin){
            throw new JeecgBootException("Bạn không phải là quản trị viên của tổ chức hiện tại, không thể xóa người dùng!");
        }
        //step2 Trong trạng thái nghỉ việc và không còn tổ chức khác thì có thể xóa
        SysUserTenant sysUserTenant = userTenantMapper.getUserTenantByTenantId(userId, tenantId);
        if(null == sysUserTenant || !CommonConstant.USER_TENANT_QUIT.equals(sysUserTenant.getStatus())){
            throw new JeecgBootException("Người dùng chưa nghỉ việc, không được phép xóa!"); 
        }
        List<Integer> tenantIdsByUserId = userTenantMapper.getTenantIdsByUserId(userId);
        if(CollectionUtils.isNotEmpty(tenantIdsByUserId) && tenantIdsByUserId.size()>0){
            throw new JeecgBootException("Người dùng vẫn còn tổ chức chưa rời khỏi, không thể xóa!");
        }
        //step3 Người dùng được tạo trong ngày và người tạo có thể xóa
        SysUser sysUserData = userService.getById(userId);
        if(!sysUserData.getCreateBy().equals(user.getUsername())){
            throw new JeecgBootException("Bạn không phải là người tạo ra người dùng này, không thể xóa!");
        }
        
        // Giải thích logic mã: 【QQYUN-11839] Xóa người dùng mà cần nhập mật khẩu của người dùng bị xóa, logic này có đúng không? Không phải nên là mật khẩu của quản trị viên sao---
        this.verifyCreateTimeAndPassword(sysUserData,password);

        //step5 Xóa logic người dùng
        userService.deleteUser(userId);
        //step6 Xóa người dùng thực sự
        userService.removeLogicDeleted(Collections.singletonList(userId));
    }

    /**
     * Xác minh thời gian tạo và mật khẩu
     * 
     * @param sysUser
     * @param password
     */
    private void verifyCreateTimeAndPassword(SysUser sysUser,String password) {
        if(null == sysUser){
            throw new JeecgBootException("Người dùng này không tồn tại, không thể xóa!");
        }
        //step1 Xác minh thời gian tạo
        //Người dùng đang đăng nhập
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Date createTime = sysUser.getCreateTime();
        boolean sameDay = DateUtils.isSameDay(createTime, new Date());
        if(!sameDay){
            throw new JeecgBootException("Người dùng không được tạo hôm nay, không thể xóa!");
        }
        //step2 Xác minh mật khẩu
        //Lấy người dùng admin
        SysUser adminUser = userService.getById(user.getId());
        String passwordEncode = PasswordUtil.encrypt(adminUser.getUsername(), password, adminUser.getSalt());
        if(!passwordEncode.equals(adminUser.getPassword())){
            throw new JeecgBootException("Mật khẩu bạn nhập không đúng, không thể xóa người dùng này!");
        }
    }

    @Override
    public List<SysTenant> getTenantListByUserId(String userId) {
        return tenantMapper.getTenantListByUserId(userId);
    }

    @Override
    public void deleteUser(SysUser sysUser, Integer tenantId) {
        //Người dùng của người bị xóa id
        String userId = sysUser.getId();
        //Mật khẩu của người bị xóa
        String password = sysUser.getPassword();
        //Người dùng đang đăng nhập
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //step1 Phán đoán xem người dùng hiện tại có phải là người tạo tenant hiện tại hay không thì mới được xóa
        SysTenant sysTenant = this.getById(tenantId);
        if(null == sysTenant || !user.getUsername().equals(sysTenant.getCreateBy())){
            throw new JeecgBootException("Bạn không phải là người tạo tổ chức hiện tại, không thể xóa người dùng!");
        }
        //step2 Phán đoán xem ngoài tổ chức hiện tại còn tham gia tổ chức khác hay không
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId,userId);
        query.ne(SysUserTenant::getTenantId,tenantId);
        List<SysUserTenant> sysUserTenants = userTenantMapper.selectList(query);
        if(CollectionUtils.isNotEmpty(sysUserTenants)){
            throw new JeecgBootException("Người dùng này vẫn còn tồn tại trong tổ chức khác, không thể xóa người dùng!");
        }
        //step3 Xác minh thời gian tạo và mật khẩu
        SysUser sysUserData = userService.getById(userId);
        this.verifyCreateTimeAndPassword(sysUserData,password);
        //step4 Xóa người dùng thực sự
        userService.deleteUser(userId);
        userService.removeLogicDeleted(Collections.singletonList(userId));
    }

    /**
     * Thêm tất cả các gói thuộc tenant cho người dùng
     *
     * @param userId   Người dùng id
     * @param tenantId Tenant id
     */
    public void addPackUser(String userId, String tenantId) {
        //Lấy gói dịch vụ tenant theo id tenant và code của gói sản phẩm id
        List<String> packIds = sysTenantPackMapper.getPackIdByPackCodeAndTenantId(oConvertUtils.getInt(tenantId));
        if (CollectionUtil.isNotEmpty(packIds)) {
            for (String packId : packIds) {
                SysTenantPackUser sysTenantPackUser = new SysTenantPackUser();
                sysTenantPackUser.setUserId(userId);
                sysTenantPackUser.setTenantId(oConvertUtils.getInt(tenantId));
                sysTenantPackUser.setPackId(packId);
                sysTenantPackUser.setStatus(CommonConstant.STATUS_1_INT);
                try {
                    this.addTenantPackUser(sysTenantPackUser);
                } catch (Exception e) {
                    log.warn("Thêm gói sản phẩm cho người dùng thất bại, nguyên nhân:" + e.getMessage());
                }
            }
        }
    }
}
