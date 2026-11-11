package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserPosition;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserPositionMapper;
import org.jeecg.modules.system.service.ISysUserPositionService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: Bảng quan hệ người dùng - vị trí công việc
 * @Author: jeecg-boot
 * @Date: 2023-02-14
 * @Version: V1.0
 */
@Service
public class SysUserPositionServiceImpl extends ServiceImpl<SysUserPositionMapper, SysUserPosition> implements ISysUserPositionService {

    @Autowired
    private SysUserPositionMapper sysUserPositionMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public IPage<SysUser> getPositionUserList(Page<SysUser> page, String positionId) {
        return page.setRecords(sysUserPositionMapper.getPositionUserList(page, positionId));
    }

    @Override
    public void saveUserPosition(String userIds, String positionId) {
        String[] userIdArray = userIds.split(SymbolConstant.COMMA);
        //Người dùng đã tồn tại
        StringBuilder userBuilder = new StringBuilder();
        for (String userId : userIdArray) {
            //Lấy thông tin thành viên có tồn tại trong chức vụ hay không
            Long count = sysUserPositionMapper.getUserPositionCount(userId, positionId);
            if (count == 0) {
                //Chèn vào bảng quan hệ người dùng-chức vụ
                SysUserPosition userPosition = new SysUserPosition();
                userPosition.setPositionId(positionId);
                userPosition.setUserId(userId);
                sysUserPositionMapper.insert(userPosition);
            } else {
                userBuilder.append(userId).append(SymbolConstant.COMMA);
            }
        }
        //Nếu id người dùng tồn tại, nghĩa là đã tồn tại trong bảng quan hệ người dùng - vị trí,Nhắc người dùng đã tồn tại
        String uIds = userBuilder.toString();
        if (oConvertUtils.isNotEmpty(uIds)) {
            //Truy vấn danh sách người dùng
            List<SysUser> sysUsers = userMapper.selectBatchIds(Arrays.asList(uIds.split(SymbolConstant.COMMA)));
            String realnames = sysUsers.stream().map(SysUser::getRealname).collect(Collectors.joining(SymbolConstant.COMMA));
            throw new JeecgBootException(realnames + "đã tồn tại trong vị trí công việc này");
        }
    }

    @Override
    public void removeByPositionId(String positionId) {
        sysUserPositionMapper.removeByPositionId(positionId);
    }

    @Override
    public void removePositionUser(String userIds, String positionId) {
        String[] userIdArray = userIds.split(SymbolConstant.COMMA);
		sysUserPositionMapper.removePositionUser(Arrays.asList(userIdArray),positionId);
    }

}
