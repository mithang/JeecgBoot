package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeecg.dingtalk.api.base.JdtBaseAPI;
import com.jeecg.dingtalk.api.core.response.Response;
import com.jeecg.dingtalk.api.core.vo.AccessToken;
import com.jeecg.dingtalk.api.user.JdtUserAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysThirdAccountMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Description: Bảng tài khoản đăng nhập bên thứ ba
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysThirdAccountServiceImpl extends ServiceImpl<SysThirdAccountMapper, SysThirdAccount> implements ISysThirdAccountService {
    
    @Autowired
    private  SysThirdAccountMapper sysThirdAccountMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Value("${justauth.type.DINGTALK.client-id:}")
    private String dingTalkClientId;   
    @Value("${justauth.type.DINGTALK.client-secret:}")
    private String dingTalkClientSecret;
    
    @Override
    public void updateThirdUserId(SysUser sysUser,String thirdUserUuid) {
        //Sửa bảng tài khoản đăng nhập bên thứ ba để thêm người dùng id
        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
        query.eq(SysThirdAccount::getThirdUserUuid,thirdUserUuid);
        //Khi quét mã đăng nhập để cập nhật người dùng, lúc tạo mới sẽ lưu tenant mặc định; lúc cập nhật cũng cần truy vấn theo tenant mặc định. Trong cùng một công ty, UUID là giống nhau, các ứng dụng khác nhau cần phân biệt tenant.
        query.eq(SysThirdAccount::getTenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        SysThirdAccount account = sysThirdAccountMapper.selectOne(query);
        SysThirdAccount sysThirdAccount = new SysThirdAccount();
        sysThirdAccount.setSysUserId(sysUser.getId());
        //Truy vấn bảng đăng nhập bên thứ ba theo id người dùng hiện tại và phương thức đăng nhập
        LambdaQueryWrapper<SysThirdAccount> thirdQuery = new LambdaQueryWrapper<>();
        thirdQuery.eq(SysThirdAccount::getSysUserId,sysUser.getId());
        thirdQuery.eq(SysThirdAccount::getThirdType,account.getThirdType());
        thirdQuery.eq(SysThirdAccount::getThirdUserUuid,thirdUserUuid);
        thirdQuery.eq(SysThirdAccount::getTenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        SysThirdAccount sysThirdAccounts = sysThirdAccountMapper.selectOne(thirdQuery);
        if(sysThirdAccounts!=null){
            sysThirdAccount.setThirdUserId(sysThirdAccounts.getThirdUserId());
            sysThirdAccountMapper.deleteById(sysThirdAccounts.getId());
        }
        //Cập nhật bảng tài khoản người dùng sys_user_id
        sysThirdAccountMapper.update(sysThirdAccount,query);
    }
    
    @Override
    public SysUser createUser(String phone, String thirdUserUuid, Integer tenantId) {
       //Truy vấn bên thứ ba trước, lấy phương thức đăng nhập
        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
        query.eq(SysThirdAccount::getThirdUserUuid,thirdUserUuid);
        query.eq(SysThirdAccount::getTenantId,tenantId);
        SysThirdAccount account = sysThirdAccountMapper.selectOne(query);
        //Truy vấn cơ sở dữ liệu theo tên người dùng xem đã tồn tại chưa
        SysUser userByName = sysUserMapper.getUserByName(thirdUserUuid);
        if(null!=userByName){
            //Nếu tài khoản tồn tại thì tự động thêm một dấu thời gian
            String format = DateUtils.yyyymmddhhmmss.get().format(new Date());
            thirdUserUuid = thirdUserUuid + format;
        }
        //Thêm người dùng
        SysUser user = new SysUser();
        user.setActivitiSync(CommonConstant.ACT_SYNC_1);
        user.setDelFlag(CommonConstant.DEL_FLAG_0);
        user.setStatus(1);
        user.setUsername(thirdUserUuid);
        user.setPhone(phone);
        //Đặt mật khẩu ban đầu
        String salt = oConvertUtils.randomGen(8);
        user.setSalt(salt);
        String passwordEncode = PasswordUtil.encrypt(user.getUsername(), "123456", salt);
        user.setPassword(passwordEncode);
        user.setRealname(account.getRealname());
        user.setAvatar(account.getAvatar());
        String s = this.saveThirdUser(user);
        //Cập nhật userId của bảng tài khoản bên thứ ba của người dùng userId
        SysThirdAccount sysThirdAccount = new SysThirdAccount();
        sysThirdAccount.setSysUserId(s);
        sysThirdAccount.setTenantId(tenantId);
        sysThirdAccountMapper.update(sysThirdAccount,query);
        return user;
    }
    
    public String saveThirdUser(SysUser sysUser) {
        //Lưu người dùng
        String userid = UUIDGenerator.generate();
        sysUser.setId(userid);
        sysUserMapper.insert(sysUser);
        //Lấy vai trò bên thứ ba
        SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "third_role"));
        //Lưu vai trò người dùng
        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(sysRole.getId());
        userRole.setUserId(userid);
        sysUserRoleMapper.insert(userRole);
        return userid;
    }

    @Override
    public SysThirdAccount getOneBySysUserId(String sysUserId, String thirdType) {
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        log.info("getSysUserId: {} ,getThirdType: {}",sysUserId,thirdType);
        queryWrapper.eq(SysThirdAccount::getSysUserId, sysUserId);
        queryWrapper.eq(SysThirdAccount::getThirdType, thirdType);
        return super.getOne(queryWrapper);
    }

    @Override
    public SysThirdAccount getOneByThirdUserId(String thirdUserId, String thirdType) {
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAccount::getThirdUserId, thirdUserId);
        queryWrapper.eq(SysThirdAccount::getThirdType, thirdType);
        return super.getOne(queryWrapper);
    }

    //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
    @Override
    public List<SysThirdAccount> listBySysUserIds(List<String> sysUserIds, String thirdType) {
        if (sysUserIds == null || sysUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysThirdAccount> qw = new LambdaQueryWrapper<>();
        qw.in(SysThirdAccount::getSysUserId, sysUserIds);
        qw.eq(SysThirdAccount::getThirdType, thirdType);
        return list(qw);
    }
    //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------

    @Override
    public List<SysThirdAccount> listThirdUserIdByUsername(String[] sysUsernameArr, String thirdType, Integer tenantId) {
        return sysThirdAccountMapper.selectThirdIdsByUsername(sysUsernameArr, thirdType,tenantId);
    }

    @Override
    public SysThirdAccount saveThirdUser(ThirdLoginModel tlm, Integer tenantId) {
        SysThirdAccount user = new SysThirdAccount();
        user.setDelFlag(CommonConstant.DEL_FLAG_0);
        user.setStatus(1);
        user.setThirdType(tlm.getSource());
        user.setAvatar(tlm.getAvatar());
        user.setRealname(tlm.getUsername());
        user.setThirdUserUuid(tlm.getUuid());
        user.setTenantId(tenantId);
        //=============begin Xác định nếu là trường hợp DingTalk thì cần tra ra id người dùng bên thứ ba, dùng khi gửi mẫu==========
        if(CommonConstant.DINGTALK.toLowerCase().equals(tlm.getSource())){
            AccessToken accessToken = JdtBaseAPI.getAccessToken(dingTalkClientId, dingTalkClientSecret);
            Response<String> getUserIdRes = JdtUserAPI.getUseridByUnionid(tlm.getUuid(), accessToken.getAccessToken());
            if (getUserIdRes.isSuccess()) {
                user.setThirdUserId(getUserIdRes.getResult());
            }else{
                user.setThirdUserId(tlm.getUuid());
            }
        //=============end Xác định nếu là trường hợp DingTalk thì cần tra ra id người dùng bên thứ ba, dùng khi gửi mẫu==========
        }else{
            user.setThirdUserId(tlm.getUuid());
        }
        super.save(user);
        return user;
    }

    @Override
    public SysThirdAccount bindThirdAppAccountByUserId(SysThirdAccount sysThirdAccount) {
        String thirdUserUuid = sysThirdAccount.getThirdUserUuid();
        String thirdType = sysThirdAccount.getThirdType();
        //Lấy người dùng đang đăng nhập
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Người dùng bên thứ ba hiện tại đã bị người dùng khác liên kết
        SysThirdAccount oneByThirdUserId = this.getOneByUuidAndThirdType(thirdUserUuid, thirdType,CommonConstant.TENANT_ID_DEFAULT_VALUE, null);
        if(null != oneByThirdUserId){
            //Nếu không rỗng và bảng bên thứ ba khớp với người dùng đang đăng nhập thì trả về trực tiếp
            if(oConvertUtils.isNotEmpty(oneByThirdUserId.getSysUserId()) && oneByThirdUserId.getSysUserId().equals(sysUser.getId())){
                return oneByThirdUserId;
            }else if(oConvertUtils.isNotEmpty(oneByThirdUserId.getSysUserId())){
                //Nếu id người dùng trong bảng bên thứ ba không rỗng thì nghĩa là đã liên kết rồi
                throw new JeecgBootException("Tài khoản QiaoQiaoYun này đã bị tài khoản bên thứ ba khác liên kết,Vui lòng hủy liên kết hoặc liên kết tài khoản QiaoQiaoYun khác");
            }else{
                //Cập nhật thông tin người dùng trong bảng bên thứ ba id
                oneByThirdUserId.setSysUserId(sysUser.getId());
                oneByThirdUserId.setThirdType(thirdType);
                sysThirdAccountMapper.updateById(oneByThirdUserId);
                return oneByThirdUserId;
            }
        }else{
            throw new JeecgBootException("Liên kết tài khoản thất bại,Vui lòng thử lại sau");
        }
    }

    @Override
    public SysThirdAccount getOneByUuidAndThirdType(String unionid, String thirdType,Integer tenantId,String thirdUserId) {
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAccount::getThirdType, thirdType);
        // Giải thích logic mã: Nếu id người dùng bên thứ ba rỗng thì không đi theo logic truy vấn người dùng bên thứ ba, vì đăng nhập quét mã third_user_id là duy nhất, không có trường hợp trùng lặp---
        if(oConvertUtils.isNotEmpty(thirdUserId)){
            queryWrapper.and((wrapper) ->wrapper.eq(SysThirdAccount::getThirdUserUuid,unionid).or().eq(SysThirdAccount::getThirdUserId,thirdUserId));
        }else{
            queryWrapper.eq(SysThirdAccount::getThirdUserUuid, unionid);
        }
        queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
        return super.getOne(queryWrapper);
    }

}
