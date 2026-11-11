package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeecg.dingtalk.api.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.dto.PushMessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.constant.enums.NoticeTypeEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.message.enums.RangeDateEnum;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppFeishuServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.util.XssUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.CommonConstant.ANNOUNCEMENT_SEND_STATUS_1;

/**
 * @Title: Controller
 * @Description: Bảng thông báo hệ thống
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/annountCement")
@Slf4j
public class SysAnnouncementController {
	@Autowired
	private ISysAnnouncementService sysAnnouncementService;
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Resource
    private WebSocket webSocket;
	@Autowired
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
    ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	ThirdAppFeishuServiceImpl feishuService;
	@Autowired
	private SysBaseApiImpl sysBaseApi;
	@Autowired
	@Lazy
	private RedisUtil redisUtil;
	@Autowired
	public RedisTemplate redisTemplate;
	//Định nghĩa lỗi thông thường
	private static final String SPECIAL_CHAR_ERROR = "Lưu thất bại:Nội dung tin nhắn chứa ký tự đặc biệt mà cơ sở dữ liệu không hỗ trợ, vui lòng kiểm tra và sửa nội dung!!";
	private static final String CONTENT_TOO_LONG_ERROR = "Lưu thất bại:Nội dung tin nhắn vượt quá giới hạn độ dài tối đa, vui lòng rút ngắn nội dung!";
	private static final String DEFAULT_ERROR = "Thao tác thất bại, vui lòng thử lại sau hoặc liên hệ quản trị viên!";
	/**
	 * Bộ nhớ đệm thông báo
	 */
	String ANNO_CACHE_KEY = "sys:cache:announcement";
	/**
	 * QQYUN-5072 Mở tin nhắn thông báo trực tuyến hơi chậm
	 */
	public static ExecutorService cachedThreadPool = new ShiroThreadPoolExecutor(0, 1024,60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	public static ExecutorService completeNoteThreadPool = new ShiroThreadPoolExecutor(0, 1024,60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	/**
	  * Truy vấn danh sách phân trang
	 * @param sysAnnouncement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequiresPermissions("system:sysAnnouncement:list")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysAnnouncement>> queryPageList(SysAnnouncement sysAnnouncement,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysAnnouncement.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		Result<IPage<SysAnnouncement>> result = new Result<IPage<SysAnnouncement>>();
		sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		QueryWrapper<SysAnnouncement> queryWrapper = QueryGenerator.initQueryWrapper(sysAnnouncement, req.getParameterMap());
		Page<SysAnnouncement> page = new Page<SysAnnouncement>(pageNo,pageSize);
		IPage<SysAnnouncement> pageList = sysAnnouncementService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	  *   Thêm
	 * @param sysAnnouncement
	 * @return
	 */
	@RequiresPermissions("system:sysAnnouncement:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysAnnouncement> add(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		try {
			// Giải thích logic mã: priority Whitelist enum, ngăn chặn lưu payload bất hợp pháp vào sys_announcement.priority kích hoạt dạng lưu trữ XSS
			String pri = sysAnnouncement.getPriority();
			if (pri != null && !pri.isEmpty()
					&& !CommonConstant.PRIORITY_H.equals(pri)
					&& !CommonConstant.PRIORITY_M.equals(pri)
					&& !CommonConstant.PRIORITY_L.equals(pri)) {
				return result.error500("priority Tham số không hợp lệ, chỉ cho phép H/M/L");
			}
			// Giải thích logic mã: Vấn đề xử lý tấn công XSS trong tiêu đề
			String title = XssUtils.scriptXss(sysAnnouncement.getTitile());
			sysAnnouncement.setTitile(title);
			//update-begin---author:liusq ---date:2025-04-13  for：【issues/9521] Trường msgContent rich text chưa được lọc XSS, tồn tại lỗ hổng XSS lưu trữ-----------
			String msgContent = XssUtils.richTextXss(sysAnnouncement.getMsgContent());
			sysAnnouncement.setMsgContent(msgContent);
			//update-end---author:liusq ---date:2025-04-13  for：【issues/9521] Trường msgContent rich text chưa được lọc XSS, tồn tại lỗ hổng XSS lưu trữ-----------
			// 【Kiểm tra bảo mật】Kiểm tra tên tệp đính kèm, ngăn chặn tấn công path traversal
			SsrfFileTypeFilter.checkPathTraversalBatch(sysAnnouncement.getFiles());
			sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
            //Chưa phát hành
			sysAnnouncement.setSendStatus(CommonSendStatus.UNPUBLISHED_STATUS_0);
            //Trạng thái quy trình
			sysAnnouncement.setBpmStatus("1");
            sysAnnouncement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
			sysAnnouncementService.saveAnnouncement(sysAnnouncement);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500(determineErrorMessage(e));
		}
		return result;
	}

	/**
	  *  Chỉnh sửa
	 * @param sysAnnouncement
	 * @return
	 */
	@RequiresPermissions("system:sysAnnouncement:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysAnnouncement> eidt(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncementEntity = sysAnnouncementService.getById(sysAnnouncement.getId());
		try{
			if(sysAnnouncementEntity==null) {
				result.error500("Không tìm thấy thực thể tương ứng");
			}else {
				// Giải thích logic mã: priority Whitelist enum, ngăn chặn lưu payload bất hợp pháp vào sys_announcement.priority kích hoạt dạng lưu trữ XSS
				String pri = sysAnnouncement.getPriority();
				if (pri != null && !pri.isEmpty()
						&& !CommonConstant.PRIORITY_H.equals(pri)
						&& !CommonConstant.PRIORITY_M.equals(pri)
						&& !CommonConstant.PRIORITY_L.equals(pri)) {
					return result.error500("priority Tham số không hợp lệ, chỉ cho phép H/M/L");
				}
				// Giải thích logic mã: Vấn đề xử lý tấn công XSS trong tiêu đề
				String title = XssUtils.scriptXss(sysAnnouncement.getTitile());
				sysAnnouncement.setTitile(title);
				//update-begin---author:liusq ---date:2025-04-13  for：【issues/9521] Trường msgContent rich text chưa được lọc XSS, tồn tại lỗ hổng XSS lưu trữ-----------
				String msgContent = XssUtils.richTextXss(sysAnnouncement.getMsgContent());
				sysAnnouncement.setMsgContent(msgContent);
				//update-end---author:liusq ---date:2025-04-13  for：【issues/9521] Trường msgContent rich text chưa được lọc XSS, tồn tại lỗ hổng XSS lưu trữ-----------
				// 【Kiểm tra bảo mật】Kiểm tra tên tệp đính kèm, ngăn chặn tấn công path traversal
				SsrfFileTypeFilter.checkPathTraversalBatch(sysAnnouncement.getFiles());
				sysAnnouncement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
				boolean ok = sysAnnouncementService.upDateAnnouncement(sysAnnouncement);
				//TODO Trả về false nghĩa là gì?
				if(ok) {
					result.success("Chỉnh sửa thành công!");
				}
			}
		} catch (Exception e) {
			result.error500(determineErrorMessage(e));
		}

		return result;
	}
	/**
	  *  Chỉnh sửa đơn giản
	 * @param sysAnnouncement
	 * @return
	 */
	//@RequiresPermissions("system:sysAnnouncement:editIzTop")
	@RequestMapping(value = "/editIzTop", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysAnnouncement> editIzTop(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncementEntity = sysAnnouncementService.getById(sysAnnouncement.getId());
		if(sysAnnouncementEntity==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			Integer izTop = sysAnnouncement.getIzTop();
			sysAnnouncementEntity.setIzTop(oConvertUtils.getInt(izTop,CommonConstant.IZ_TOP_0));
			sysAnnouncementService.updateById(sysAnnouncementEntity);
			result.success("Chỉnh sửa thành công!");
		}
		return result;
	}

	/**
	  *   Xóa theo id
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:sysAnnouncement:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
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
	@RequiresPermissions("system:sysAnnouncement:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Không nhận dạng được tham số!");
		}else {
			String[] id = ids.split(",");
			for(int i=0;i<id.length;i++) {
				SysAnnouncement announcement = sysAnnouncementService.getById(id[i]);
				announcement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
				sysAnnouncementService.updateById(announcement);
			}
			result.success("Xóa thành công!");
		}
		return result;
	}

	/**
	  * Tìm kiếm theo id
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysAnnouncement> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			result.setResult(sysAnnouncement);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 *	 Cập nhật thao tác phát hành
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:sysAnnouncement:doReleaseData")
	@RequestMapping(value = "/doReleaseData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReleaseData(@RequestParam(name="id",required=true) String id, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
            //Đang phát hành
			sysAnnouncement.setSendStatus(CommonSendStatus.PUBLISHED_STATUS_1);
			sysAnnouncement.setSendTime(new Date());
			String currentUserName = JwtUtil.getUserNameByToken(request);
			sysAnnouncement.setSender(currentUserName);
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
            if(oConvertUtils.isEmpty(sysAnnouncement.getNoticeType())){
                sysAnnouncement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
            }
			if(ok) {
				result.success("Đẩy thông báo hệ thống thành công");
				if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					// Bổ sung quan hệ giữa thông báo và người dùng
					sysAnnouncementService.batchInsertSysAnnouncementSend(sysAnnouncement.getId(), sysAnnouncement.getTenantId());
					
					// Đẩy thông báo websocket
					JSONObject obj = new JSONObject();
			    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
					obj.put(CommonConstant.NOTICE_TYPE, sysAnnouncement.getNoticeType());
			    	webSocket.sendMessage(obj.toJSONString());
					//update-begin-author:liusq---date:2025-11-13--for: JHHB-827 [Tin nhắn phê duyệt] Thiết bị di động cần có đẩy tin -Đẩy toàn bộ
					PushMessageDTO pushMessageDTO = new PushMessageDTO();
					pushMessageDTO.setTitle(sysAnnouncement.getTitile());
					pushMessageDTO.setPushType(CommonConstant.MSG_TYPE_ALL);
					pushMessageDTO.setContent(sysAnnouncement.getMsgAbstract());
					sysBaseApi.uniPushMsgToUser(pushMessageDTO);
					//update-begin-author:liusq---date:2025-11-13--for: JHHB-827 [Tin nhắn phê duyệt] Thiết bị di động cần có đẩy tin -Đẩy toàn bộ
				}else {
					// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
					String userId = sysAnnouncement.getUserIds();
					String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
					String anntId = sysAnnouncement.getId();
					Date refDate = new Date();
					JSONObject obj = new JSONObject();
			    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
                    obj.put(CommonConstant.NOTICE_TYPE, sysAnnouncement.getNoticeType());
			    	webSocket.sendMessage(userIds, obj.toJSONString());
					//update-begin-author:liusq---date:2025-11-13--for: JHHB-827 [Tin nhắn phê duyệt] Thiết bị di động cần có đẩy tin
					PushMessageDTO pushMessageDTO = new PushMessageDTO();
					pushMessageDTO.setTitle(sysAnnouncement.getTitile());
					pushMessageDTO.setUserIds(Arrays.asList(userIds));
					pushMessageDTO.setContent(sysAnnouncement.getMsgAbstract());
					sysBaseApi.uniPushMsgToUser(pushMessageDTO);
					//update-begin-author:liusq---date:2025-11-13--for: JHHB-827 [Tin nhắn phê duyệt] Thiết bị di động cần có đẩy tin
				}
				try {
					// Đồng bộ thông báo tin nhắn của WeChat doanh nghiệp, DingTalk
					Response<String> dtResponse = dingtalkService.sendActionCardMessage(sysAnnouncement, null, true);
					wechatEnterpriseService.sendTextCardMessage(sysAnnouncement, null,true);
					feishuService.sendAnnouncementMessage(sysAnnouncement, null, true);

					if (dtResponse != null && dtResponse.isSuccess()) {
						String taskId = dtResponse.getResult();
						sysAnnouncement.setDtTaskId(taskId);
						sysAnnouncementService.updateById(sysAnnouncement);
					}
				} catch (Exception e) {
					log.error("Gửi đồng bộ tin nhắn App bên thứ ba thất bại:", e);
				}
			}
		}

		return result;
	}

	/**
	 *	 Cập nhật thao tác thu hồi
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:sysAnnouncement:doReovkeData")
	@RequestMapping(value = "/doReovkeData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReovkeData(@RequestParam(name="id",required=true) String id, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
            //Hủy phát hành
			sysAnnouncement.setSendStatus(CommonSendStatus.REVOKE_STATUS_2);
			sysAnnouncement.setCancelTime(new Date());
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
			if(ok) {
				result.success("Thông báo hệ thống này đã thu hồi thành công");
				if (oConvertUtils.isNotEmpty(sysAnnouncement.getDtTaskId())) {
					try {
						dingtalkService.recallMessage(sysAnnouncement.getDtTaskId());
					} catch (Exception e) {
						log.error("Thu hồi tin nhắn của APP bên thứ ba thất bại:", e);
					}
				}
			}
		}

		return result;
	}

	/**
	 * @Chức năng: bổ sung dữ liệu người dùng và trả về tin nhắn hệ thống
	 * @return
	 */
	@RequestMapping(value = "/listByUser", method = RequestMethod.GET)
	public Result<Map<String, Object>> listByUser(@RequestParam(required = false, defaultValue = "5") Integer pageSize, HttpServletRequest request) {
		long start = System.currentTimeMillis();
		Result<Map<String,Object>> result = new Result<Map<String,Object>>();
		Map<String,Object> sysMsgMap = new HashMap(5);
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();


		// Lấy ngày đầu tiên của tháng trước (chỉ tra thông báo trong gần hai tháng)
		Date lastMonthStartDay = DateRangeUtils.getLastMonthStartDay();
		log.info("-----Truy vấn thông báo chưa đọc nhận được trong gần hai tháng-----，ngày đầu tiên của 2 tháng gần nhất：{}", lastMonthStartDay);
		
//		//Bổ sung dữ liệu đẩy (bảng quan hệ giữa người dùng và thông báo)
//		completeNoteThreadPool.execute(()->{
//			sysAnnouncementService.completeAnnouncementSendInfo();
//		});
		
		// 2.Truy vấn tin nhắn hệ thống chưa đọc của người dùng
		Page<SysAnnouncement> anntMsgList = new Page<SysAnnouncement>(0, pageSize);
        //Tin nhắn thông báo
		anntMsgList = sysAnnouncementService.querySysCementPageByUserId(anntMsgList,userId,"1",null, lastMonthStartDay);
		sysMsgMap.put("anntMsgList", anntMsgList.getRecords());
		sysMsgMap.put("anntMsgTotal", anntMsgList.getTotal());

		log.info("begin Lấy thông báo hệ thống của người dùng trong gần 2 tháng (Thông báo)" + (System.currentTimeMillis() - start) + "Mili giây");
		
        //Tin nhắn hệ thống
		Page<SysAnnouncement> sysMsgList = new Page<SysAnnouncement>(0, pageSize);
		sysMsgList = sysAnnouncementService.querySysCementPageByUserId(sysMsgList,userId,"2",null, lastMonthStartDay);
		sysMsgMap.put("sysMsgList", sysMsgList.getRecords());
		sysMsgMap.put("sysMsgTotal", sysMsgList.getTotal());

		log.info("end Lấy thông báo hệ thống trong 2 tháng của người dùng (Tin nhắn hệ thống)" + (System.currentTimeMillis() - start) + "Mili giây");
		
		result.setSuccess(true);
		result.setResult(sysMsgMap);
		return result;
	}


	/**
	 * Lấy số lượng thông báo tin nhắn chưa đọc
	 *
	 * @return
	 */
	@RequestMapping(value = "/getUnreadMessageCount", method = RequestMethod.GET)
	public Result<Map<String, Integer>> getUnreadMessageCount(@RequestParam(required = false, defaultValue = "5") Integer pageSize, HttpServletRequest request) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();

		// Lấy ngày đầu tiên của tháng trước (chỉ tra thông báo trong gần hai tháng)
		Date lastMonthStartDay = DateRangeUtils.getLastMonthStartDay();
		log.debug(" ------Truy vấn số lượng tin nhắn thông báo chưa đọc nhận được trong gần hai tháng------，ngày đầu tiên của 2 tháng gần nhất：{}", lastMonthStartDay);
        // Giải thích logic mã: 【QQYUN-12162] Cải tạo dự án OA, tách hệ thống tin nhắn nặng; hiện các tin nhắn đang ở chung với nhau, cần tách theo danh mục---
        Map<String,Integer> unreadMessageCount = new HashMap<>();
        //Số lượng tin nhắn hệ thống
        Integer systemCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay, NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
        unreadMessageCount.put("systemCount",systemCount);
        //Số lượng quy trình
        Integer flowCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay, NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue());
        unreadMessageCount.put("flowCount",flowCount);
        //Số lượng tệp
        Integer fileCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay,  NoticeTypeEnum.NOTICE_TYPE_FILE.getValue());
        unreadMessageCount.put("fileCount",fileCount);
        //Số lượng kế hoạch lịch trình
        Integer planCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay,  NoticeTypeEnum.NOTICE_TYPE_PLAN.getValue());
        unreadMessageCount.put("planCount",planCount);
        Integer count = systemCount + flowCount + fileCount + planCount;
        unreadMessageCount.put("count",count);
        return Result.ok(unreadMessageCount);
	}


    /**
     * Xuất excel
     *
     * @param request
     */
    @RequiresPermissions("system:sysAnnouncement:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysAnnouncement sysAnnouncement,HttpServletRequest request) {
        // Step.1 Lắp ráp điều kiện truy vấn
        LambdaQueryWrapper<SysAnnouncement> queryWrapper = new LambdaQueryWrapper<SysAnnouncement>(sysAnnouncement);
        //Step.2 AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		queryWrapper.eq(SysAnnouncement::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        List<SysAnnouncement> pageList = sysAnnouncementService.list(queryWrapper);
        //Tên file xuất
        mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách thông báo hệ thống");
        mv.addObject(NormalExcelConstants.CLASS, SysAnnouncement.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Dữ liệu danh sách thông báo hệ thống", "Người xuất:"+user.getRealname(), "Thông tin xuất"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:sysAnnouncement:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysAnnouncement> listSysAnnouncements = ExcelImportUtil.importExcel(file.getInputStream(), SysAnnouncement.class, params);
                for (SysAnnouncement sysAnnouncementExcel : listSysAnnouncements) {
                	if(sysAnnouncementExcel.getDelFlag()==null){
                		sysAnnouncementExcel.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
					}
                	if(oConvertUtils.isEmpty(sysAnnouncementExcel.getIzTop())){
                		sysAnnouncementExcel.setIzTop(CommonConstant.IZ_TOP_0);
					}
                    sysAnnouncementService.save(sysAnnouncementExcel);
                }
                return Result.ok("Nhập tệp thành công! Số dòng dữ liệu:" + listSysAnnouncements.size());
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return Result.error("Nhập tệp thất bại!");
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("Nhập tệp thất bại!");
    }
	/**
	 *Đồng bộ tin nhắn
	 * @param anntId
	 * @return
	 */
	//@RequiresPermissions("system:sysAnnouncement:syncNotic")
	@RequestMapping(value = "/syncNotic", method = RequestMethod.GET)
	public Result<SysAnnouncement> syncNotic(@RequestParam(name="anntId",required=false) String anntId, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		JSONObject obj = new JSONObject();
		if(StringUtils.isNotBlank(anntId)){
			SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(anntId);
			if(sysAnnouncement==null) {
				result.error500("Không tìm thấy thực thể tương ứng");
			}else {
				if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
					webSocket.sendMessage(obj.toJSONString());
				}else {
					// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
					String userId = sysAnnouncement.getUserIds();
					if(oConvertUtils.isNotEmpty(userId)){
						String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
						obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
						obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
						obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
						webSocket.sendMessage(userIds, obj.toJSONString());
					}
				}
			}
		}else{
			obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
			obj.put(WebsocketConst.MSG_TXT, "Đặt đã đọc hàng loạt");
			webSocket.sendMessage(obj.toJSONString());
		}
		return result;
	}

	/**
	 * Trang xem chi tiết thông báo (dùng cho APP bên thứ ba)
	 * @param modelAndView
	 * @param id
	 * @return
	 */
    @GetMapping("/show/{id}")
    public ModelAndView showContent(ModelAndView modelAndView, @PathVariable("id") String id, HttpServletRequest request) {
        SysAnnouncement announcement = sysAnnouncementService.getById(id);
        if (announcement != null) {
            boolean tokenOk = false;
            try {
                // Xác minh tính hợp lệ của Token
                tokenOk = TokenUtils.verifyToken(request, sysBaseApi, redisUtil);
            } catch (Exception ignored) {
            }
            // Xác định có truyền Token hay không và Token có hợp lệ không; nếu đã truyền thì không áp giới hạn xem, trả về trực tiếp
            // Nếu token không hợp lệ thì thực hiện giới hạn xem: chỉ được xem những nội dung đã phát hành
            if (tokenOk || ANNOUNCEMENT_SEND_STATUS_1.equals(announcement.getSendStatus())) {
				LoginUser user = sysBaseApi.getUserByName(announcement.getSender());
				if(oConvertUtils.isNotEmpty(user)){
					announcement.setSender(user.getRealname());
				}
				modelAndView.addObject("data", announcement);
                modelAndView.setViewName("announcement/showContent");
                return modelAndView;
            }
        }
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

	/**
	 * [Dùng cho vue3] Truy vấn danh sách tin nhắn
	 * @param fromUser
	 * @param busType
	 * @param starFlag
	 * @param msgCategory
	 * @param beginDate
	 * @param endDate
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/vue3List", method = RequestMethod.GET)
	public Result<List<SysAnnouncement>> vue3List(@RequestParam(name="fromUser", required = false) String fromUser,
												  @RequestParam(name="busType", required = false) String busType,
												  @RequestParam(name="starFlag", required = false) String starFlag,
												  @RequestParam(name="msgCategory", required = false) String msgCategory,
                                                  @RequestParam(name="rangeDateKey", required = false) String rangeDateKey,
                                                  @RequestParam(name="beginDate", required = false) String beginDate, 
												  @RequestParam(name="endDate", required = false) String endDate,
												  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo, 
												  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                  @RequestParam(name= "noticeType", required = false) String noticeType) {
		long calStartTime = System.currentTimeMillis(); // Ghi lại thời gian bắt đầu
		
		// 1, lấy điều kiện truy vấn theo ngày, thời gian bắt đầu và thời gian kết thúc
		Date beginTime = null, endTime = null;
		if (RangeDateEnum.ZDY.getKey().equals(rangeDateKey)) {
			// Truy vấn phạm vi ngày tùy chỉnh
			if (oConvertUtils.isNotEmpty(beginDate)) {
				beginTime = DateUtils.parseDatetime(beginDate);
			}
			if (oConvertUtils.isNotEmpty(endDate)) {
				endTime = DateUtils.parseDatetime(endDate);
			}
		} else {
			// Truy vấn theo đoạn ngày
			Date[] arr = RangeDateEnum.getRangeArray(rangeDateKey);
			if (arr != null) {
				beginTime = arr[0];
				endTime = arr[1];	
			}
		}
		
		// 2, truy vấn tin nhắn thông báo của người dùng theo điều kiện
		List<SysAnnouncement> ls = this.sysAnnouncementService.querySysMessageList(pageSize, pageNo, fromUser, starFlag,busType, msgCategory, beginTime, endTime, noticeType);

		// 3, đặt tin nhắn của trang hiện tại thành đã đọc
		if (!CollectionUtils.isEmpty(ls)) {
			// Đánh dấu đã đọc
			String readed = "1";
			List<String> annoceIdList = ls.stream().filter(item -> !readed.equals(item.getReadFlag())).map(item -> item.getId()).collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(annoceIdList)) {
				cachedThreadPool.execute(() -> {
					sysAnnouncementService.updateReaded(annoceIdList);
				});
			}
		}
		
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		webSocket.sendMessage(sysUser.getId(), obj.toJSONString());

		// 4, thống kê thời gian thực thi hiệu năng
		long calEndTime = System.currentTimeMillis(); // Ghi lại thời gian kết thúc
		long duration = calEndTime - calStartTime; // Tính thời gian thực hiện
		//System.out.println("Thời gian thực hiện:" + duration + " Mili giây");
		
		return Result.ok(ls);
	}


    /**
     * Theo id người dùng, lấy thời gian gửi tin nhắn mới nhất(Thời gian tạo)
     * @param userId
     * @return
     */
	@GetMapping("/getLastAnnountTime")
	public Result<Page<SysAnnouncementSend>> getLastAnnountTime(@RequestParam(name = "userId") String userId,@RequestParam(name="noticeType",required = false) String noticeType){
		Result<Page<SysAnnouncementSend>> result = new Result<>();
		//----------------------------------------------------------------------------------------
		// step.1 Giao diện này quá chậm, có thể dùng phương án lưu đệm một giờ
		String keyString = String.format(CommonConstant.CACHE_KEY_USER_LAST_ANNOUNT_TIME_1HOUR, userId) + "_" + noticeType;
		if (redisTemplate.hasKey(keyString)) {
			log.debug("[SysAnnouncementSend Redis] Truy vấn qua bộ nhớ đệm Redis thời gian người dùng nhận thông báo hệ thống lần cuối,userId={}", userId);
			Page<SysAnnouncementSend> pageList = (Page<SysAnnouncementSend>) redisTemplate.opsForValue().get(keyString);
			result.setSuccess(true);
			result.setResult(pageList);
			return result;
		}
		//----------------------------------------------------------------------------------------

		Page<SysAnnouncementSend> page = new Page<>(1,1);
        LambdaQueryWrapper<SysAnnouncementSend> query = new LambdaQueryWrapper<>();
        query.eq(SysAnnouncementSend::getUserId,userId);
        //Chỉ truy vấn dữ liệu thông báo của tháng trước và tháng này
		query.ne(SysAnnouncementSend::getCreateTime, DateRangeUtils.getLastMonthStartDay());
        query.select(SysAnnouncementSend::getCreateTime); // Nâng cao hiệu quả truy vấn
        query.orderByDesc(SysAnnouncementSend::getCreateTime);
        Page<SysAnnouncementSend> pageList = sysAnnouncementSendService.page(page, query);

		//----------------------------------------------------------------------------------------
		if (pageList != null && pageList.getSize() > 0) {
			// step.3 Giữ bộ nhớ đệm redis trong 1 giờ
			redisTemplate.opsForValue().set(keyString, pageList, 3600, TimeUnit.SECONDS);
		}
		//----------------------------------------------------------------------------------------

		result.setSuccess(true);
		result.setResult(pageList);
        return result;
    }

	/**
	 * Xóa tất cả tin nhắn chưa đọc của người dùng hiện tại
	 * @return
	 */
	@PostMapping("/clearAllUnReadMessage")
    public Result<String> clearAllUnReadMessage(){
		sysAnnouncementService.clearAllUnReadMessage();
		return Result.ok("Xóa tin nhắn chưa đọc thành công");
	}

	/**
	 * Thêm số lần truy cập
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/addVisitsNumber", method = RequestMethod.GET)
	public Result<?> addVisitsNumber(@RequestParam(name="id",required=true) String id) {
		int count = oConvertUtils.getInt(redisUtil.get(ANNO_CACHE_KEY+id),0) + 1;
		redisUtil.set(ANNO_CACHE_KEY+id, count);

		if (count % 5 == 0) {
			cachedThreadPool.execute(() -> {
				sysAnnouncementService.updateVisitsNum(id, count);
			});
			// Đặt lại số lần truy cập
			redisUtil.del(ANNO_CACHE_KEY+id);
		}
		return Result.ok("Số lần truy cập thông báo+1 lần");
	}

	/**
	 * Tải file hàng loạt
	 * @param id
	 * @param request
	 * @param response
	 */
	@GetMapping("/downLoadFiles")
	public void downLoadFiles(@RequestParam(name="id") String id,
							  HttpServletRequest request,
							  HttpServletResponse response){
		sysAnnouncementService.downLoadFiles(id,request,response);
	}
	/**
	 * Xác định thông báo lỗi thân thiện theo thông tin ngoại lệ
	 */
	private String determineErrorMessage(Exception e) {
		String errorMsg = e.getMessage();
		if (isSpecialCharacterError(errorMsg)) {
			return SPECIAL_CHAR_ERROR;
		} else if (isContentTooLongError(errorMsg)) {
			return CONTENT_TOO_LONG_ERROR;
		} else {
			return DEFAULT_ERROR;
		}
	}
	/**
	 * Kiểm tra có phải lỗi ký tự đặc biệt hay không
	 */
	private boolean isSpecialCharacterError(String errorMsg) {
		return errorMsg != null
				&& errorMsg.contains("Incorrect string value")
				&& errorMsg.contains("column 'msg_content'");
	}

	/**
	 * Kiểm tra có phải lỗi nội dung quá dài hay không
	 */
	private boolean isContentTooLongError(String errorMsg) {
		return errorMsg != null
				&& errorMsg.contains("Data too long for column 'msg_content'");
	}
}
