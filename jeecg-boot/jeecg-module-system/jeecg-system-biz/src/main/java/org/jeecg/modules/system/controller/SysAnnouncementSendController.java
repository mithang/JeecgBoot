package org.jeecg.modules.system.controller;

import java.util.Arrays;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.model.AnnouncementSendModel;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

 /**
 * @Title: Controller
 * @Description: Bảng đánh dấu đã đọc thông báo của người dùng
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/sysAnnouncementSend")
@Slf4j
public class SysAnnouncementSendController {
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Autowired
	private WebSocket webSocket;

	/**
	  * Truy vấn danh sách phân trang
	 * @param sysAnnouncementSend
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	// update---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] API này cả frontend và phía server đều chưa dùng, tạm dừng toàn bộ bằng chú thích, tránh liệt kê trái phép bản ghi gửi tin nhắn của toàn bộ người dùng
	// @GetMapping(value = "/list")
	// public Result<IPage<SysAnnouncementSend>> queryPageList(SysAnnouncementSend sysAnnouncementSend,
									  // @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  // @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  // HttpServletRequest req) {
		// Result<IPage<SysAnnouncementSend>> result = new Result<IPage<SysAnnouncementSend>>();
		// QueryWrapper<SysAnnouncementSend> queryWrapper = new QueryWrapper<SysAnnouncementSend>(sysAnnouncementSend);
		// Page<SysAnnouncementSend> page = new Page<SysAnnouncementSend>(pageNo,pageSize);
		// //Xử lý logic sắp xếp
		// String column = req.getParameter("column");
		// String order = req.getParameter("order");
//
		// if(oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
			// if(DataBaseConstant.SQL_ASC.equals(order)) {
				// queryWrapper.orderByAsc(SqlInjectionUtil.getSqlInjectSortField(column));
			// }else {
				// queryWrapper.orderByDesc(SqlInjectionUtil.getSqlInjectSortField(column));
			// }
		// }
		// IPage<SysAnnouncementSend> pageList = sysAnnouncementSendService.page(page, queryWrapper);
		// //log.info("Truy vấn trang hiện tại: "+pageList.getCurrent());
		// //log.info("Truy vấn số lượng trang hiện tại:"+pageList.getSize());
		// //log.info("Số lượng kết quả truy vấn:"+pageList.getRecords().size());
		// //log.info("Tổng số dữ liệu:"+pageList.getTotal());
		// result.setSuccess(true);
		// result.setResult(pageList);
		// return result;
	// }
	// update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj】Giao diện không sử dụng được ghi chú dừng

	// update---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] API này cả frontend và phía server đều chưa dùng, tạm dừng toàn bộ bằng chú thích, tránh giả mạo bản ghi tin nhắn cho người khác
	// @PostMapping(value = "/add")
	// public Result<SysAnnouncementSend> add(@RequestBody SysAnnouncementSend sysAnnouncementSend) {
		// Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
		// try {
			// sysAnnouncementSendService.save(sysAnnouncementSend);
			// result.success("Thêm thành công!");
		// } catch (Exception e) {
			// log.error(e.getMessage(),e);
			// result.error500("Thao tác thất bại");
		// }
		// return result;
	// }
	// update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj】Giao diện không sử dụng được ghi chú dừng
	
	/**
	  *  Chỉnh sửa
	 * @param sysAnnouncementSend
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<SysAnnouncementSend> eidt(@RequestBody SysAnnouncementSend sysAnnouncementSend) {
		Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
		SysAnnouncementSend sysAnnouncementSendEntity = sysAnnouncementSendService.getById(sysAnnouncementSend.getId());
		if(sysAnnouncementSendEntity==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] Chỉ được sửa bản ghi gửi tin nhắn của chính mình, tránh vượt quyền sửa bản ghi của người khác-----------
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if(!loginUser.getId().equals(sysAnnouncementSendEntity.getUserId())){
				return result.error500("Không có quyền sửa bản ghi tin nhắn của người khác");
			}
			//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] Chỉ được sửa bản ghi gửi tin nhắn của chính mình, tránh vượt quyền sửa bản ghi của người khác-----------
			boolean ok = sysAnnouncementSendService.updateById(sysAnnouncementSend);
			//TODO Trả về false nghĩa là gì?
			if(ok) {
				result.success("Thao tác thành công!");
			}
		}
		
		return result;
	}
	
	/**
	  *   Xóa theo id
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<SysAnnouncementSend> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
        //Lấy số lượng người dùng hiện tại đã đọc theo id người dùng và id của bảng đọc thông báo
		long count = sysAnnouncementSendService.getReadCountByUserId(id);
		if(0 == count) {
			result.error500("Xóa thất bại, dữ liệu này không tồn tại hoặc chưa được đánh dấu là “đã đọc”");
		}else {
			boolean ok = sysAnnouncementSendService.removeById(id);
			if(ok) {
				result.success("Xóa thành công!");
			}
		}
		
		return result;
	}
	
	/**
	  *  Xóa hàng loạt
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysAnnouncementSend> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Không nhận dạng được tham số!");
		}else {
			this.sysAnnouncementSendService.deleteBatchByIds(ids);
			result.success("Xóa tin nhắn đã đọc thành công!!");
		}
		return result;
	}
	
	/**
	  * Tìm kiếm theo id
	 * @param id
	 * @return
	 */
	// update---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] API này cả frontend và phía server đều chưa dùng, tạm dừng toàn bộ bằng chú thích, tránh xem trái phép bản ghi gửi tin nhắn của người khác
	// @GetMapping(value = "/queryById")
	// public Result<SysAnnouncementSend> queryById(@RequestParam(name="id",required=true) String id) {
		// Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
		// SysAnnouncementSend sysAnnouncementSend = sysAnnouncementSendService.getById(id);
		// if(sysAnnouncementSend==null) {
			// result.error500("Không tìm thấy thực thể tương ứng");
		// }else {
			// result.setResult(sysAnnouncementSend);
			// result.setSuccess(true);
		// }
		// return result;
	// }
	// update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj】Giao diện không sử dụng được ghi chú dừng
	
	/**
	 * @Chức năng: Cập nhật trạng thái đọc tin nhắn hệ thống của người dùng
	 * @param json
	 * @return
	 */
	@PutMapping(value = "/editByAnntIdAndUserId")
	public Result<SysAnnouncementSend> editById(@RequestBody JSONObject json) {
		Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
		String anntId = json.getString("anntId");
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();
		LambdaUpdateWrapper<SysAnnouncementSend> updateWrapper = new UpdateWrapper().lambda();
		updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
		updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
		// Giải thích logic mã: Cách viết lỗ hổng sql tồn tại trong module hệ thống
		updateWrapper.eq(SysAnnouncementSend::getAnntId,anntId);
		updateWrapper.eq(SysAnnouncementSend::getUserId,userId);
		//updateWrapper.last("where annt_id ='"+anntId+"' and user_id ='"+userId+"'");
		SysAnnouncementSend announcementSend = new SysAnnouncementSend();
		sysAnnouncementSendService.update(announcementSend, updateWrapper);
		result.setSuccess(true);
		return result;
	}
	
	/**
	 * @Chức năng: Lấy tin nhắn của tôi
	 * @return
	 */
	@GetMapping(value = "/getMyAnnouncementSend")
	public Result<IPage<AnnouncementSendModel>> getMyAnnouncementSend(AnnouncementSendModel announcementSendModel,
			@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
		    @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
		Result<IPage<AnnouncementSendModel>> result = new Result<IPage<AnnouncementSendModel>>();
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();
		announcementSendModel.setUserId(userId);
		announcementSendModel.setPageNo((pageNo-1)*pageSize);
		announcementSendModel.setPageSize(pageSize);
		// Giải thích logic mã: 【TV360X-545]Danh sách tin nhắn của tôi không thể truy vấn theo khoảng thời gian---
		if(StringUtils.isNotEmpty(announcementSendModel.getSendTimeBegin())){
			announcementSendModel.setSendTimeBegin(announcementSendModel.getSendTimeBegin() + " 00:00:00");
		}
		if(StringUtils.isNotEmpty(announcementSendModel.getSendTimeBegin())){
			announcementSendModel.setSendTimeEnd(announcementSendModel.getSendTimeEnd() + " 23:59:59");
		}
		Page<AnnouncementSendModel> pageList = new Page<AnnouncementSendModel>(pageNo,pageSize);
		pageList = sysAnnouncementSendService.getMyAnnouncementSendPage(pageList, announcementSendModel);
		result.setResult(pageList);
		result.setSuccess(true);
		return result;
	}

	/**
	 * @Chức năng: Đánh dấu đã đọc tất cả
	 * @return
	 */
	@PutMapping(value = "/readAll")
	public Result<SysAnnouncementSend> readAll() {
		Result<SysAnnouncementSend> result = new Result<SysAnnouncementSend>();
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();
		LambdaUpdateWrapper<SysAnnouncementSend> updateWrapper = new UpdateWrapper().lambda();
		updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
		updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
		updateWrapper.eq(SysAnnouncementSend::getUserId,userId);
		//updateWrapper.last("where user_id ='"+userId+"'");
		SysAnnouncementSend announcementSend = new SysAnnouncementSend();
		sysAnnouncementSendService.update(announcementSend, updateWrapper);
		JSONObject socketParams = new JSONObject();
		socketParams.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
		webSocket.sendMessage(socketParams.toJSONString());
		result.setSuccess(true);
		result.setMessage("Đã đọc tất cả");
		return result;
	}


	 /**
	  * Lấy nội dung tin nhắn theo ID bản ghi gửi tin nhắn
	  * @param sendId
	  * @return
	  */
	 @GetMapping(value = "/getOne")
	 public Result<AnnouncementSendModel> getOne(@RequestParam(name="sendId",required=true) String sendId) {
		 //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] Kiểm tra quyền sở hữu trước khi lấy nội dung tin nhắn theo sendId, chỉ được xem tin nhắn do chính mình nhận-----------
		 LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 SysAnnouncementSend sendEntity = sysAnnouncementSendService.getById(sendId);
		 if(sendEntity==null || !loginUser.getId().equals(sendEntity.getUserId())){
			 return Result.error("Không có quyền xem tin nhắn này");
		 }
		 //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-63v7-3c98-c3mj] Kiểm tra quyền sở hữu trước khi lấy nội dung tin nhắn theo sendId, chỉ được xem tin nhắn do chính mình nhận-----------
		 AnnouncementSendModel model = sysAnnouncementSendService.getOne(sendId);
		 //update-begin---author:jeecg---for: 【Bảo mật GHSA-fp4f-qhf5-hqj8】Kiểm tra quyền sở hữu tin nhắn, chỉ được xem thư nội bộ của bản thân---
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 if (model != null && !sysUser.getId().equals(model.getUserId())) {
			 return Result.error("Không có quyền xem tin nhắn này");
		 }
		 //update-end---author:jeecg---for: 【Bảo mật GHSA-fp4f-qhf5-hqj8】Kiểm tra quyền sở hữu tin nhắn---
		 return Result.ok(model);
	 }

	 /**
	  * Sửa trạng thái đọc theo loại nghiệp vụ và ID
	  * @param busType
	  * @return
	  */
	 @GetMapping(value = "/updateSysAnnounReadFlag")
	 public Result<AnnouncementSendModel> updateSysAnnounReadFlag(
			 @RequestParam(name="busId",required=true) String busId,
			 @RequestParam(name="busType",required=false) String busType) {
		 //Cập nhật trạng thái đã đọc
		 boolean updateFlag = sysAnnouncementSendService.updateReadFlagByBusId(busId,busType);
		 if(updateFlag){
			//Làm mới số lượng chưa đọc
			JSONObject obj = new JSONObject();
			obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
		 }
		 return Result.ok();
	 }
}
