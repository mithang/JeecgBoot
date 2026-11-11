package org.jeecg.modules.system.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.model.AnnouncementSendModel;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: Bảng đánh dấu đã đọc thông báo của người dùng
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
@Service
public class SysAnnouncementSendServiceImpl extends ServiceImpl<SysAnnouncementSendMapper, SysAnnouncementSend> implements ISysAnnouncementSendService {

	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;

    @Autowired
    private SysAnnouncementMapper sysAnnouncementMapper;

	@Override
	public Page<AnnouncementSendModel> getMyAnnouncementSendPage(Page<AnnouncementSendModel> page,
			AnnouncementSendModel announcementSendModel) {
		 return page.setRecords(sysAnnouncementSendMapper.getMyAnnouncementSendList(page, announcementSendModel));
	}

	@Override
	public AnnouncementSendModel getOne(String sendId) {
		return sysAnnouncementSendMapper.getOne(sendId);
	}

    /**
     * Lấy số lượng đã đọc của người dùng hiện tại
     * 
     * @param id
     * @return
     */
    @Override
    public long getReadCountByUserId(String id) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        return sysAnnouncementSendMapper.getReadCountByUserId(id, sysUser.getId());
    }

    /**
     * Xóa hàng loạt số lượng đã đọc theo nhiều id
     * 
     * @param ids
     */
    @Override
    public void deleteBatchByIds(String ids) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Lấy tất cả dữ liệu đã đọc theo id người dùng và id của bảng đọc
        List<String> sendIds = sysAnnouncementSendMapper.getReadAnnSendByUserId(Arrays.asList(ids.split(SymbolConstant.COMMA)),sysUser.getId());
        if(CollectionUtil.isNotEmpty(sendIds)){
            this.removeByIds(sendIds);
        }
    }

    /**
     * Cập nhật trạng thái đã đọc theo busId
     * @param busId
     * @param busType
     */
    @Override
    public boolean updateReadFlagByBusId(String busId, String busType) {
        boolean updateFlag = false;
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        List<String> unReadAnnouncementsIds = sysAnnouncementSendMapper.getUnReadAnnByBusAndUserId(busId,busType,userId);
        if(CollectionUtil.isNotEmpty(unReadAnnouncementsIds)){
            sysAnnouncementSendMapper.updateReaded(userId, unReadAnnouncementsIds);
            updateFlag =  true;
        }
        return updateFlag;
    }
}
