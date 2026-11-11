package org.jeecg.modules.system.service.impl;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Bảng thông báo hệ thống
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement> implements ISysAnnouncementService {
	/**
	 * Bổ sung dữ liệu đổi sang chế độ backend
	 */
	public static ExecutorService completeNoteThreadPool = new ThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	
	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;
	@Resource
	private SysUserMapper sysUserMapper;
	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveAnnouncement(SysAnnouncement sysAnnouncement) {
		if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
			sysAnnouncementMapper.insert(sysAnnouncement);
		}else {
			// 1.Chèn bản ghi vào bảng thông báo
			sysAnnouncementMapper.insert(sysAnnouncement);
			// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
			String userId = sysAnnouncement.getUserIds();
            // Giải thích logic mã: [issues/5503]【Thông báo】Không thể nhận thông báo
			if(StringUtils.isNotBlank(userId) && userId.endsWith(",")){
				userId = userId.substring(0, (userId.length()-1));
			}
			String[] userIds = userId.split(",");
			String anntId = sysAnnouncement.getId();
			Date refDate = new Date();
			for(int i=0;i<userIds.length;i++) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(userIds[i]);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				announcementSend.setReadTime(refDate);
				sysAnnouncementSendMapper.insert(announcementSend);
			}
		}
	}
	
	/**
	 * @Chức năng: Chỉnh sửa thông tin tin nhắn
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean upDateAnnouncement(SysAnnouncement sysAnnouncement) {
		// 1.Cập nhật dữ liệu bảng thông tin hệ thống
		sysAnnouncementMapper.updateById(sysAnnouncement);
		String userId = sysAnnouncement.getUserIds();
		if(oConvertUtils.isNotEmpty(userId)&&sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_UESR)) {
			// 2.Bổ sung dữ liệu người dùng thông báo mới
			String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
			String anntId = sysAnnouncement.getId();
			Date refDate = new Date();
			for(int i=0;i<userIds.length;i++) {
				LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
				queryWrapper.eq(SysAnnouncementSend::getAnntId, anntId);
				queryWrapper.eq(SysAnnouncementSend::getUserId, userIds[i]);
				List<SysAnnouncementSend> announcementSends=sysAnnouncementSendMapper.selectList(queryWrapper);
				if(announcementSends.size()<=0) {
					SysAnnouncementSend announcementSend = new SysAnnouncementSend();
					announcementSend.setAnntId(anntId);
					announcementSend.setUserId(userIds[i]);
					announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					announcementSend.setReadTime(refDate);
					sysAnnouncementSendMapper.insert(announcementSend);
				}
			}
			// 3. Xóa dữ liệu người dùng thông báo thừa
			Collection<String> delUserIds = Arrays.asList(userIds);
			LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
			queryWrapper.notIn(SysAnnouncementSend::getUserId, delUserIds);
			queryWrapper.eq(SysAnnouncementSend::getAnntId, anntId);
			sysAnnouncementSendMapper.delete(queryWrapper);
		}
		return true;
	}

    /**
     * Sau khi quy trình thực thi xong, lưu thông báo tin nhắn
     * @param title Tiêu đề
     * @param msgContent Nội dung thông tin
     */
	@Override
	public void saveSysAnnouncement(String title, String msgContent) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender("JEECG BOOT");
		announcement.setPriority(CommonConstant.PRIORITY_L);
		announcement.setMsgType(CommonConstant.MSG_TYPE_ALL);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		sysAnnouncementMapper.insert(announcement);
	}

	@Override
	public Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> page, String userId, String msgCategory, Integer tenantId, Date beginDate) {
		if (page.getSize() == -1) {
			return page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(null, userId, msgCategory,tenantId,beginDate));
		} else {
			return page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(page, userId, msgCategory,tenantId,beginDate));
		}
	}

	@Override
	public Integer getUnreadMessageCountByUserId(String userId, Date beginDate, String noticeType) {
		return sysAnnouncementMapper.getUnreadMessageCountByUserId(userId, beginDate, noticeType);
	}

	@Override
	public void completeAnnouncementSendInfo() {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();
		List<String> announcementIds = this.getNotSendedAnnouncementlist(userId);
		List<SysAnnouncementSend> sysAnnouncementSendList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(announcementIds)) {
			for (String commentId : announcementIds) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(commentId);
				announcementSend.setUserId(userId);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendList.add(announcementSend);
			}
		}
		if (!CollectionUtils.isEmpty(sysAnnouncementSendList)) {
			sysAnnouncementSendService.saveBatch(sysAnnouncementSendList);
		}
	}

	@Override
	public void batchInsertSysAnnouncementSend(String commentId, Integer tenantId) {
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isNotEmpty(tenantId)) {
			log.info("Bổ sung dữ liệu quan hệ giữa thông báo và người dùng, tenant ID = {}", tenantId);
		} else {
			tenantId = null;
		}

		//update-begin---author:Claude ---date:2026-08-05---for: 【Thông báo】Sau khi thu hồi và phát lại cho toàn thể nhân viên sys_announcement_send Phát sinh bản ghi trùng lặp, xóa trước rồi chèn theo anntId---
		// Xóa trước các bản ghi gửi đã có của thông báo đó theo id thông báo, tránh sau khi thu hồi rồi phát hành lại thì cùng một người dùng phát sinh nhiều bản ghi chưa đọc
		LambdaQueryWrapper<SysAnnouncementSend> delQw = new LambdaQueryWrapper<>();
		delQw.eq(SysAnnouncementSend::getAnntId, commentId);
		sysAnnouncementSendService.remove(delQw);
		//update-end---author:Claude ---date:2026-08-05---for: 【Thông báo】Sau khi thu hồi và phát lại cho toàn thể nhân viên sys_announcement_send Phát sinh bản ghi trùng lặp, xóa trước rồi chèn theo anntId---

		List<String> userIdList = sysUserMapper.getTenantUserIdList(tenantId);
		List<SysAnnouncementSend> sysAnnouncementSendList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(userIdList)) {
			for (String userId : userIdList) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(commentId);
				announcementSend.setUserId(userId);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendList.add(announcementSend);
			}
		}
		if (!CollectionUtils.isEmpty(sysAnnouncementSendList)) {
			log.info("Bổ sung đầy đủ dữ liệu quan hệ giữa thông báo và người dùng,sysAnnouncementSendList size = {}", sysAnnouncementSendList.size());
			sysAnnouncementSendService.saveBatch(sysAnnouncementSendList);
		}
	}

	@Override
	public List<SysAnnouncement> querySysMessageList(int pageSize, int pageNo, String fromUser, String starFlag, String busType, String msgCategory, Date beginDate, Date endDate, String noticeType) {
//		//1. Bổ sung đầy đủ dữ liệu bảng send
//		completeNoteThreadPool.execute(()->{
//			completeAnnouncementSendInfo();
//		});
		
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		log.debug(" Lấy người đăng nhập LoginUser id: {}", sysUser.getId());
		Page<SysAnnouncement> page = new Page<SysAnnouncement>(pageNo,pageSize);
		List<SysAnnouncement> list = baseMapper.queryAllMessageList(page, sysUser.getId(), fromUser, starFlag, busType, msgCategory,beginDate, endDate, noticeType);
		return list;
	}

	@Override
	public void updateReaded(List<String> annoceIdList) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		sysAnnouncementSendMapper.updateReaded(sysUser.getId(), annoceIdList);
	}

	@Override
	public void clearAllUnReadMessage() {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		sysAnnouncementSendMapper.clearAllUnReadMessage(sysUser.getId());
	}

	/**
	 * Truy vấn thông báo chưa đọc của người dùng, điều chỉnh cách viết để tránh SQL injection
	 * @param userId
	 * @return
	 */

	@Override
	public List<String> getNotSendedAnnouncementlist(String userId) {
		return sysAnnouncementMapper.getNotSendedAnnouncementlist(new Date(), userId);
	}

	/**
	 * Cập nhật lượt truy cập
	 * @param id
	 * @param increaseCount
	 */
	@Override
	public void updateVisitsNum(String id, int increaseCount) {
		SysAnnouncement sysAnnouncement = sysAnnouncementMapper.selectById(id);
		if (oConvertUtils.isNotEmpty(sysAnnouncement)) {
			int visits = oConvertUtils.getInt(sysAnnouncement.getVisitsNum(), 0);
			int totalValue = increaseCount + visits;
			sysAnnouncement.setVisitsNum(totalValue);
			sysAnnouncementMapper.updateById(sysAnnouncement);
			log.info("Thông báo:{} Số lần truy cập+1, tổng số lượt truy cập:{}", sysAnnouncement.getTitile(), sysAnnouncement.getVisitsNum());
		}
	}

	/**
	 * Tải file hàng loạt
	 * @param id
	 * @param request
	 * @param response
	 */
	@Override
	public void downLoadFiles(String id, HttpServletRequest request, HttpServletResponse response) {
		// Kiểm tra tham số
		if (oConvertUtils.isEmpty(id)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// Lấy thông tin bài viết
		SysAnnouncement sysAnnouncement = this.baseMapper.selectById(id);
		if (oConvertUtils.isEmpty(sysAnnouncement)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		//Đặt header phản hồi HTTP: chuẩn bị tải tệp xuống
		response.reset();
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/force-download");
		ZipArchiveOutputStream zous = null;
		try {
			// Tạo tên tệp ZIP: sử dụng tiêu đề bài viết+Dùng timestamp để tránh trùng tên
			String title = sysAnnouncement.getTitile() + new Date().getTime();
			String zipName = URLEncoder.encode( title + ".zip", "UTF-8").replaceAll("\\+", "%20");
			response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + zipName);
			// Tạo luồng xuất ZIP: xuất trực tiếp ra luồng phản hồi HTTP
			zous = new ZipArchiveOutputStream(response.getOutputStream());
			zous.setUseZip64(Zip64Mode.AsNeeded);// Hỗ trợ tệp lớn

			// Tải file hàng loạt
			String[] fileUrls = sysAnnouncement.getFiles().split(",");
			// Duyệt qua tất cả các tệp URL
			for (int i = 0; i < fileUrls.length; i++) {
				String fileUrl = fileUrls[i].trim();
				if (oConvertUtils.isEmpty(fileUrl)) {
					continue;
				}
				// [Kiểm tra an toàn] Ngăn tấn công duyệt đường dẫn
				SsrfFileTypeFilter.checkPathTraversal(fileUrl);
				// Tạo tên tệp trong ZIP: tránh trùng tên, thêm số thứ tự
				String fileName = FileDownloadUtils.generateFileName(fileUrl, i, fileUrls.length);
				String uploadUrl = jeecgBaseConfig.getPath().getUpload();
				// Tải xuống từng tệp và thêm vào ZIP
				FileDownloadUtils.downLoadSingleFile(fileUrl,fileName,uploadUrl, zous);
			}
			// Hoàn tất ghi ZIP
			zous.finish();
			// Làm mới bộ đệm để đảm bảo dữ liệu được gửi
			response.flushBuffer();
		} catch (IOException e) {
			log.error("Tải xuống tệp thất bại"+e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			// Đảm bảo đóng luồng, tránh rò rỉ tài nguyên
			IoUtil.close(zous);
		}
	}
}
