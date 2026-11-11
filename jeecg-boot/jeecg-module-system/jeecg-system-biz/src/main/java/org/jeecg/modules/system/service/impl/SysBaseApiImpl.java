package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import com.jeecg.dingtalk.api.core.response.Response;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.PushMessageDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.UrlMatchEnum;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.*;
import org.jeecg.common.constant.enums.*;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.online.api.IOnlineBaseExtApi;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryCondition;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.vo.*;
import org.jeecg.common.util.*;
import org.jeecg.common.util.dynamic.db.FreemarkerParseFactory;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.flow.service.IAiragFlowService;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.handle.impl.DdSendMsgHandle;
import org.jeecg.modules.message.handle.impl.EmailSendMsgHandle;
import org.jeecg.modules.message.handle.impl.FsSendMsgHandle;
import org.jeecg.modules.message.handle.impl.QywxSendMsgHandle;
import org.jeecg.modules.message.handle.impl.SmsSendMsgHandle;
import org.jeecg.modules.message.handle.impl.SystemSendMsgHandle;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.SecurityUtil;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import jakarta.annotation.Resource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: API nghiệp vụ chung tầng dưới, cung cấp cho các module độc lập khác gọi
 * @Author: scott
 * @Date:2019-4-20 
 * @Version:V1.0
 */
@Slf4j
@Service
public class SysBaseApiImpl implements ISysBaseAPI {
	/** Loại cơ sở dữ liệu của hệ thống hiện tại */
	private static String DB_TYPE = "";

	/** DingTalk/WeCom/Feishu/Thread pool gửi SMS bất đồng bộ */
	private static final ExecutorService MSG_THREAD_POOL = new ThreadPoolExecutor(
			0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

	// uniapp Địa chỉ api gọi đẩy
	@Value("${jeecg.unicloud.pushUrl:}")
	private String jeecgPushUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Lazy
	@Autowired
	private IOnlineBaseExtApi onlineBaseExtApi;

	@Autowired
	private ISysMessageTemplateService sysMessageTemplateService;
	@Resource
	private SysUserMapper userMapper;
	@Resource
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISysDictService sysDictService;
	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;
	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;
	@Resource
    private WebSocket webSocket;
	@Resource
	private SysRoleMapper roleMapper;
	@Resource
	private SysDepartMapper departMapper;
	@Resource
	private SysCategoryMapper categoryMapper;
	@Autowired
	private ISysDataSourceService dataSourceService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private ISysUserDepPostService sysUserDepPostService;
	@Resource
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;
	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
	private ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	private ThirdAppFeishuServiceImpl feishuService;
	@Autowired
	ISysCategoryService sysCategoryService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDataLogService sysDataLogService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysUserTenantService sysUserTenantService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysUserPositionService sysUserPositionService;

	@Autowired
	private IDictTableWhiteListHandler dictTableWhiteListHandler;

	@Autowired
	private ISysAnnouncementService sysAnnouncementService;

    @Autowired
    IAiragFlowService airagFlowService;

	@Override
	//@SensitiveDecode
	public LoginUser getUserByName(String username) {
		// Giải thích logic mã: VUEN-1276 [Sơ đồ quy trình v3] Bug kiểm thử 1. Qua quy trình do tôi khởi tạo hoặc instance quy trình, xem lịch sử, vấn đề xem trước sơ đồ quy trình
		if (oConvertUtils.isEmpty(username)) {
			return null;
		}
		LoginUser user = sysUserService.getEncodeUserInfo(username);

		//Khi các phương thức trong cùng một class gọi nhau, AOP giải mã khử nhạy cảm sẽ mất hiệu lực; việc lấy thông tin người dùng quá quan trọng nên tại đây dùng phương pháp giải mã gốc, không dùng@SensitiveDecodeAble Phương thức annotation
		try {
			SensitiveInfoUtil.handlerObject(user, false);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return user;
	}


	@Override
	@Cacheable(cacheNames=CommonConstant.SYS_USER_ID_MAPPING_CACHE, key="#username")
	public String getUserIdByName(String username) {
		if (oConvertUtils.isEmpty(username)) {
			return null;
		}
		String userId = userMapper.getUserIdByName(username);
		return userId;
	}
	

	@Override
	public String translateDictFromTable(String table, String text, String code, String key) {
		return sysDictService.queryTableDictTextByKey(table, text, code, key);
	}

	@Override
	public String translateDict(String code, String key) {
		return sysDictService.queryDictTextByKey(code, key);
	}

	@Override
	public List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username) {
		List<SysPermission> currentSyspermission = null;
		if(oConvertUtils.isNotEmpty(component)) {
			//1.Lấy menu thông qua thuộc tính annotation pageComponent
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag,0);
			query.eq(SysPermission::getComponent, component);
			currentSyspermission = sysPermissionMapper.selectList(query);
		}else {
			//1.Truy vấn trực tiếp menu qua địa chỉ yêu cầu của giao diện
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getMenuType,2);
			query.eq(SysPermission::getDelFlag,0);
			
			// Giải thích logic mã: Giải quyết vấn đề thứ tự tham số
			List<String> allPossiblePaths = this.getOnlinePossiblePaths(requestPath);
			log.debug("Địa chỉ menu lấy được= {}", allPossiblePaths.toString());
			if(allPossiblePaths.size()==1){
				query.eq(SysPermission::getUrl, requestPath);
			}else{
				query.in(SysPermission::getUrl, allPossiblePaths);	
			}
			
			currentSyspermission = sysPermissionMapper.selectList(query);
			//2.Không tìm thấy thì lấy menu qua URL khớp tùy chỉnh
			if(currentSyspermission==null || currentSyspermission.size()==0) {
				//Lấy menu thông qua quy tắc khớp URL tùy chỉnh (thực hiện cấu hình quy tắc quyền dữ liệu qua menu, thực chất là kiểm soát quy tắc dữ liệu đối với API lấy dữ liệu)
				String userMatchUrl = UrlMatchEnum.getMatchResultByUrl(requestPath);
				LambdaQueryWrapper<SysPermission> queryQserMatch = new LambdaQueryWrapper<SysPermission>();
				// Giải thích logic mã:  online Nếu menu được cấu hình thành menu cấp một thì không tra được quyền hạn, hủy bỏ menuType = 1
				//queryQserMatch.eq(SysPermission::getMenuType, 1);
				queryQserMatch.eq(SysPermission::getDelFlag, 0);
				queryQserMatch.eq(SysPermission::getUrl, userMatchUrl);
				if(oConvertUtils.isNotEmpty(userMatchUrl)){
					currentSyspermission = sysPermissionMapper.selectList(queryQserMatch);
				}
			}
			//3.Không tìm thấy, tiếp tục lấy menu bằng cách khớp regex
			if(currentSyspermission==null || currentSyspermission.size()==0) {
				//Khớp cấu hình quyền hạn bằng regex
				String regUrl = getRegexpUrl(requestPath);
				if(regUrl!=null) {
					currentSyspermission = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getMenuType,2).eq(SysPermission::getUrl, regUrl).eq(SysPermission::getDelFlag,0));
				}
			}
		}
		if(currentSyspermission!=null && currentSyspermission.size()>0){
			List<SysPermissionDataRuleModel> dataRules = new ArrayList<SysPermissionDataRuleModel>();
			for (SysPermission sysPermission : currentSyspermission) {
				// Giải thích logic mã: Mã quy tắc quyền dữ liệu không đúng chuẩn, dự án tồn tại tên package và tên class giống nhau #722
				List<SysPermissionDataRule> temp = sysPermissionDataRuleService.queryPermissionDataRules(username, sysPermission.getId());
				if(temp!=null && temp.size()>0) {
					//dataRules.addAll(temp);
					dataRules = oConvertUtils.entityListToModelList(temp,SysPermissionDataRuleModel.class);
				}
			}
			return dataRules;
		}
		return null;
	}

	/**
	 * Khớp địa chỉ do frontend truyền qua, khớp thành công thì trả về địa chỉ regex
	 * AntPathMatcher Khớp địa chỉ
	 *()* Khớp 0 hoặc nhiều ký tự
	 *()**Khớp 0 hoặc nhiều thư mục
	 */
	private String getRegexpUrl(String url) {
		List<String> list = sysPermissionMapper.queryPermissionUrlWithStar();
		if(list!=null && list.size()>0) {
			for (String p : list) {
				PathMatcher matcher = new AntPathMatcher();
				if(matcher.match(p, url)) {
					return p;
				}
			}
		}
		return null;
	}

	@Override
	public SysUserCacheInfo getCacheUser(String username) {
		SysUserCacheInfo info = new SysUserCacheInfo();
		info.setOneDepart(true);
		LoginUser user = this.getUserByName(username);

//		try {
//			//Khử nhạy cảm khi các phương thức trong cùng một lớp gọi nhau@SensitiveDecodeAble Xử lý AOP giải mã không hiệu lực
//			SensitiveInfoUtil.handlerObject(user, false);
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}

		if(user!=null) {
			info.setSysUserId(user.getId());
			info.setSysUserCode(user.getUsername());
			info.setSysUserName(user.getRealname());
			info.setSysOrgCode(user.getOrgCode());
			info.setSysOrgId(user.getOrgId());
			info.setSysRoleCode(user.getRoleCode());
		}else{
			return null;
		}
		//Nhiều phòng ban hỗ trợ truy vấn in
		List<SysDepart> list = departMapper.queryUserDeparts(user.getId());
		List<String> sysMultiOrgCode = new ArrayList<String>();
		if(list==null || list.size()==0) {
			//Người dùng hiện tại không có phòng ban
			//sysMultiOrgCode.add("0");
		}else if(list.size()==1) {
			sysMultiOrgCode.add(list.get(0).getOrgCode());
		}else {
			info.setOneDepart(false);
			for (SysDepart dpt : list) {
				sysMultiOrgCode.add(dpt.getOrgCode());
			}
		}
		info.setSysMultiOrgCode(sysMultiOrgCode);
		return info;
	}

	@Override
	public LoginUser getUserById(String id) {
		if(oConvertUtils.isEmpty(id)) {
			return null;
		}
		LoginUser loginUser = new LoginUser();
		SysUser sysUser = userMapper.selectById(id);
		if(sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(sysUser, loginUser);
		//Loại bỏ thông tin nhạy cảm của người dùng
		loginUser.setPassword(null);
		loginUser.setRelTenantIds(null);
		loginUser.setDepartIds(null);
		return loginUser;
	}

	@Override
	public List<String> getRolesByUsername(String username) {
		return sysUserRoleMapper.getRoleByUserName(username);
	}
	
	@Override
	public List<String> getRolesByUserId(String userId) {
		return sysUserRoleMapper.getRoleCodeByUserId(userId);
	}

	@Override
	public List<String> getDepartIdsByUsername(String username) {
		List<SysDepart> list = sysDepartService.queryDepartsByUsername(username);
		List<String> result = new ArrayList<>(list.size());
		for (SysDepart depart : list) {
			result.add(depart.getId());
		}
		return result;
	}
	
	@Override
	public List<String> getDepartIdsByUserId(String userId) {
		return sysDepartService.queryDepartsByUserId(userId);
	}

	@Override
	public Map<String, List<String>> getDepartIdsByUserIds(Collection<String> userIds) {
		return sysDepartService.queryDepartIdsByUserIds(userIds);
	}

	@Override
	public Set<String> getDepartParentIdsByUsername(String username) {
		List<SysDepart> list = sysDepartService.queryDepartsByUsername(username);
		Set<String> result = new HashSet<>(list.size());
		for (SysDepart depart : list) {
			result.add(depart.getParentId());
		}
		return result;
	}

	@Override
	public Set<String> getDepartParentIdsByDepIds(Set<String> depIds) {
		LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<SysDepart>().in(SysDepart::getId, depIds);
		List<SysDepart> departList = departMapper.selectList(departQuery);

		if(CollectionUtils.isEmpty(departList)){
			return null;
		}
		Set<String> parentIds = departList.stream()
				.map(SysDepart::getParentId)
				.collect(Collectors.toSet());
		return parentIds;
	}

	@Override
	public List<String> getDepartNamesByUsername(String username) {
		List<SysDepart> list = sysDepartService.queryDepartsByUsername(username);
		List<String> result = new ArrayList<>(list.size());
		for (SysDepart depart : list) {
			result.add(depart.getDepartName());
		}
		return result;
	}

	@Override
	@Cacheable(cacheNames = CacheConstant.SYS_USERS_CACHE, key = "#username + '::main_depart_info'", unless = "#result == null")
	public SysDepartModel queryMainDepartByUsername(String username) {
		if (oConvertUtils.isEmpty(username)) {
			return null;
		}
		// Truy vấn thông tin phòng ban chính theo tên người dùng
		SysDepart mainDepart = userMapper.getMainDepartByUsername(username);
		if (mainDepart == null) {
			return null;
		}

		// Sao chép thông tin phòng ban vào đối tượng model
		SysDepartModel model = new SysDepartModel();
		BeanUtils.copyProperties(mainDepart, model);

		// Đặt tên đường dẫn phòng ban
		String departPathName = sysDepartService.getDepartPathNameByOrgCode(model.getOrgCode(), null);
		model.setDepartPathName(departPathName);

		return model;
	}

	@Override
	public DictModel getParentDepartId(String departId) {
		SysDepart depart = departMapper.getParentDepartId(departId);
		DictModel model = new DictModel(depart.getId(),depart.getParentId());
		return model;
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String code) {
		return sysDictService.queryDictItemsByCode(code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String code) {
		return sysDictService.queryEnableDictItemsByCode(code);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code) {
		//【Online+Hệ thống】Thêm cơ chế kiểm soát quyền cho bảng danh mục, ý tưởng hay LOWCOD-799
		if(tableFilterSql.indexOf(SymbolConstant.SYS_VAR_PREFIX)>=0){
			tableFilterSql = QueryGenerator.getSqlRuleValue(tableFilterSql);
		}
		return sysDictService.queryTableDictItemsByCode(tableFilterSql, text, code);
	}

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return sysDictService.queryAllDepartBackDictModel();
	}

	@Override
	public void sendSysAnnouncement(MessageDTO message) {
		this.sendSysAnnouncement(message.getFromUser(),
				message.getToUser(),
				message.getTitle(),
				message.getContent(),
				message.getCategory(),
                message.getNoticeType());
		try {
			// Đồng bộ gửi tin nhắn APP bên thứ ba
			wechatEnterpriseService.sendMessage(message, true);
			dingtalkService.sendMessage(message, true);
			feishuService.sendMessage(message, true);
		} catch (Exception e) {
			log.error("Gửi đồng bộ tin nhắn App bên thứ ba thất bại!", e);
		}
	}

	@Override
	public void sendBusAnnouncement(BusMessageDTO message) {
		sendBusAnnouncement(message.getFromUser(),
				message.getToUser(),
				message.getTitle(),
				message.getContent(),
				message.getCategory(),
				message.getBusType(),
				message.getBusId(),
				message.getNoticeType());
		try {
			// Đồng bộ gửi tin nhắn APP bên thứ ba
			wechatEnterpriseService.sendMessage(message, true);
			dingtalkService.sendMessage(message, true);
			feishuService.sendMessage(message, true);
		} catch (Exception e) {
			log.error("Gửi đồng bộ tin nhắn App bên thứ ba thất bại!", e);
		}
	}

	@Override
	public void sendTemplateAnnouncement(TemplateMessageDTO message) {
		String templateCode = message.getTemplateCode();
		String title = message.getTitle();
		Map<String,String> tmplateParam = message.getTemplateParam();
		String fromUser = message.getFromUser();
		String toUser = message.getToUser();

		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("Mẫu tin nhắn không tồn tại, mã mẫu:"+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//Tiêu đề mẫu
		title = title==null?sysSmsTemplate.getTemplateName():title;
		//Nội dung mẫu
		String content = sysSmsTemplate.getTemplateContent();
		if(tmplateParam!=null) {
			for (Map.Entry<String, String> entry : tmplateParam.entrySet()) {
				String str = "${" + entry.getKey() + "}";
				if(oConvertUtils.isNotEmpty(title)){
					title = title.replace(str, entry.getValue());
				}
				content = content.replace(str, entry.getValue());
			}
		}
		String mobileOpenUrl = null;
		if(tmplateParam!=null && oConvertUtils.isNotEmpty(tmplateParam.get(CommonConstant.MSG_HREF_URL))){
			mobileOpenUrl = tmplateParam.get(CommonConstant.MSG_HREF_URL);
		}
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(content);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(CommonConstant.MSG_CATEGORY_2);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
       // Giải thích logic mã: [QQYUN-12999]Thông báo hệ thống, thời gian thông báo hệ thống được cập nhật nhưng lại bị xếp xuống dưới
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
  		sysAnnouncementMapper.insert(announcement);
		// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}
		try {
			// Đồng bộ thông báo tin nhắn của WeChat doanh nghiệp, DingTalk
			dingtalkService.sendActionCardMessage(announcement, mobileOpenUrl, true);
			wechatEnterpriseService.sendTextCardMessage(announcement, mobileOpenUrl, true);
			feishuService.sendAnnouncementMessage(announcement, mobileOpenUrl, true);
		} catch (Exception e) {
			log.error("Gửi đồng bộ tin nhắn App bên thứ ba thất bại!", e);
		}

	}

	@Override
	public void sendBusTemplateAnnouncement(BusTemplateMessageDTO message) {
		String templateCode = message.getTemplateCode();
		String title = message.getTitle();
		Map<String,String> tmplateParam = message.getTemplateParam();
		String fromUser = message.getFromUser();
		String toUser = message.getToUser();
		String busId = message.getBusId();
		String busType = message.getBusType();

		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("Mẫu tin nhắn không tồn tại, mã mẫu:"+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//Tiêu đề mẫu
		title = title==null?sysSmsTemplate.getTemplateName():title;
		//Nội dung mẫu
		String content = sysSmsTemplate.getTemplateContent();
		if(tmplateParam!=null) {
			for (Map.Entry<String, String> entry : tmplateParam.entrySet()) {
				String str = "${" + entry.getKey() + "}";
				if (entry.getValue() != null) {
					title = title.replace(str, entry.getValue());
					content = content.replace(str, entry.getValue());
				}
			}
		}
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(content);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		// Giải thích logic mã: [QQYUN-12999]Thông báo hệ thống, thời gian thông báo hệ thống được cập nhật nhưng lại bị xếp xuống dưới
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		if(tmplateParam!=null && oConvertUtils.isNotEmpty(tmplateParam.get(CommonSendStatus.MSG_ABSTRACT_JSON))){
			announcement.setMsgAbstract(tmplateParam.get(CommonSendStatus.MSG_ABSTRACT_JSON));
		}
		String mobileOpenUrl = null;
		if(tmplateParam!=null && oConvertUtils.isNotEmpty(tmplateParam.get(CommonConstant.MSG_HREF_URL))){
			mobileOpenUrl = tmplateParam.get(CommonConstant.MSG_HREF_URL);
		}

		// Nếu truyền JSON mở rộng thì nghĩa là nghiệp vụ cá nhân hóa; nếu có remark góp ý thì đặt làm nội dung thông báo
		if(oConvertUtils.isJson(announcement.getMsgAbstract())) {
			// Lấy announcement.getMsgAbstract() trường remark
			JSONObject jsonObject = JSON.parseObject(announcement.getMsgAbstract());
			String remark = jsonObject.containsKey("remark")? jsonObject.getString("remark"): null;
			if(oConvertUtils.isNotEmpty(remark)){
				announcement.setMsgContent(remark);
			}
		}

		announcement.setMsgCategory(CommonConstant.MSG_CATEGORY_2);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		announcement.setBusId(busId);
		announcement.setBusType(busType);
		//update-begin---author:liusq ---date:2026-10-13  for：【LHZP-2388】【QiaoQiaoYun】Sau khi ủy quyền, thúc giục không nhận được thông báo hệ thống-----------
		if(SysAnnmentTypeEnum.BPM.getType().equals(busType)){
			announcement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue());
		}
		//update-end---author:liusq ---date:2026-10-13  for：【LHZP-2388】【QiaoQiaoYun】Sau khi ủy quyền, thúc giục không nhận được thông báo hệ thống-----------
		announcement.setOpenType(SysAnnmentTypeEnum.getByType(busType).getOpenType());
		announcement.setOpenPage(SysAnnmentTypeEnum.getByType(busType).getOpenPage());
		sysAnnouncementMapper.insert(announcement);
		// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}
		try {
			// Thông báo tin nhắn DingTalk
			dingtalkService.sendActionCardMessage(announcement, mobileOpenUrl, true);
			// Thông báo WeChat Doanh nghiệp
			wechatEnterpriseService.sendTextCardMessage(announcement, mobileOpenUrl, true);
			feishuService.sendAnnouncementMessage(announcement, mobileOpenUrl, true);
			// Uniapp Đẩy tin nhắn trên điện thoại di động
			PushMessageDTO pushMessageDTO = new PushMessageDTO();
			pushMessageDTO.setTitle(announcement.getTitile());
			pushMessageDTO.setContent(announcement.getMsgContent());
			pushMessageDTO.setPayload(new HashMap<>(message.getTemplateParam()));
			pushMessageDTO.setUsernames(Arrays.asList(toUser));
			this.uniPushMsgToUser(pushMessageDTO);
		} catch (Exception e) {
			log.error("Gửi đồng bộ tin nhắn App bên thứ ba thất bại!", e);
		}

	}

	@Override
	public String parseTemplateByCode(TemplateDTO templateDTO) {
		String templateCode = templateDTO.getTemplateCode();
		Map<String, String> map = templateDTO.getTemplateParam();
		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("Mẫu tin nhắn không tồn tại, mã mẫu:"+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//Nội dung mẫu
		String content = sysSmsTemplate.getTemplateContent();
		if(map!=null) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String str = "${" + entry.getKey() + "}";
				content = content.replace(str, entry.getValue());
			}
		}
		return content;
	}

	@Override
	public void updateSysAnnounReadFlag(String busType, String busId) {
		SysAnnouncement announcement = sysAnnouncementMapper.selectOne(new QueryWrapper<SysAnnouncement>().eq("bus_type",busType).eq("bus_id",busId));
		if(announcement != null){
			LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
			String userId = sysUser.getId();
			LambdaUpdateWrapper<SysAnnouncementSend> updateWrapper = new UpdateWrapper().lambda();
			updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
			updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
			updateWrapper.eq(SysAnnouncementSend::getAnntId,announcement.getId());
			updateWrapper.eq(SysAnnouncementSend::getUserId,userId);
			//updateWrapper.last("where annt_id ='"+announcement.getId()+"' and user_id ='"+userId+"'");
			SysAnnouncementSend announcementSend = new SysAnnouncementSend();
			sysAnnouncementSendMapper.update(announcementSend, updateWrapper);
		}
	}

	/**
	 * Lấy loại cơ sở dữ liệu
	 * @param dataSource
	 * @return
	 * @throws SQLException
	 */
	private String getDatabaseTypeByDataSource(DataSource dataSource) throws SQLException{
		if("".equals(DB_TYPE)) {
			Connection connection = dataSource.getConnection();
			try {
				DatabaseMetaData md = connection.getMetaData();
				String dbType = md.getDatabaseProductName().toLowerCase();
				if(dbType.indexOf(DataBaseConstant.DB_TYPE_MYSQL.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_MYSQL;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_ORACLE.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_ORACLE;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_SQLSERVER.toLowerCase())>=0||dbType.indexOf(DataBaseConstant.DB_TYPE_SQL_SERVER_BLANK)>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_SQLSERVER;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_POSTGRESQL.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_POSTGRESQL;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_MARIADB.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_MARIADB;
				}else {
					log.error("Loại cơ sở dữ liệu:[" + dbType + "]Không nhận dạng được!");
					//throw new JeecgBootException("Loại cơ sở dữ liệu:["+dbType+"]Không nhận dạng được!");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}finally {
				connection.close();
			}
		}
		return DB_TYPE;

	}

	@Override
	public List<DictModel> queryAllDict() {
		// Tìm kiếm và sắp xếp
		QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>();
		queryWrapper.orderByAsc("create_time");
		List<SysDict> dicts = sysDictService.list(queryWrapper);
		// Đóng gói thành model
		List<DictModel> list = new ArrayList<DictModel>();
		for (SysDict dict : dicts) {
			list.add(new DictModel(dict.getDictCode(), dict.getDictName()));
		}

		return list;
	}

	@Override
	public List<SysCategoryModel> queryAllSysCategory() {
		List<SysCategory> ls = categoryMapper.selectList(null);
		List<SysCategoryModel> res = oConvertUtils.entityListToModelList(ls,SysCategoryModel.class);
		return res;
	}

	@Override
	public List<DictModel> queryFilterTableDictInfo(String table, String text, String code, String filterSql) {
		return sysDictService.queryTableDictItemsByCodeAndFilter(table,text,code,filterSql);
	}

	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String[] keyArray) {
		return sysDictService.queryTableDictByKeys(table,text,code,Joiner.on(",").join(keyArray));
	}

	@Override
	public List<ComboModel> queryAllUserBackCombo() {
		List<ComboModel> list = new ArrayList<ComboModel>();
		List<SysUser> userList = userMapper.selectList(new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0));
		for(SysUser user : userList){
			ComboModel model = new ComboModel();
			model.setTitle(user.getRealname());
			model.setId(user.getId());
			model.setUsername(user.getUsername());
			list.add(model);
		}
		return list;
	}

	@Override
	public JSONObject queryAllUser(String userIds, Integer pageNo, Integer pageSize) {
		JSONObject json = new JSONObject();
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		List<ComboModel> list = new ArrayList<ComboModel>();
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		IPage<SysUser> pageList = userMapper.selectPage(page, queryWrapper);
		for(SysUser user : pageList.getRecords()){
			ComboModel model = new ComboModel();
			model.setUsername(user.getUsername());
			model.setTitle(user.getRealname());
			model.setId(user.getId());
			model.setEmail(user.getEmail());
			if(oConvertUtils.isNotEmpty(userIds)){
				String[] temp = userIds.split(",");
				for(int i = 0; i<temp.length;i++){
					if(temp[i].equals(user.getId())){
						model.setChecked(true);
					}
				}
			}
			list.add(model);
		}
		json.put("list",list);
		json.put("total",pageList.getTotal());
		return json;
	}

	public List<ComboModel> queryAllRole() {
		List<ComboModel> list = new ArrayList<ComboModel>();
		List<SysRole> roleList = roleMapper.selectList(new QueryWrapper<SysRole>());
		for(SysRole role : roleList){
			ComboModel model = new ComboModel();
			model.setTitle(role.getRoleName());
			model.setId(role.getId());
			model.setRoleCode(role.getRoleCode());
			list.add(model);
		}
		return list;
	}

    @Override
    public List<ComboModel> queryAllRole(String[] roleIds) {
        List<ComboModel> list = new ArrayList<ComboModel>();
        List<SysRole> roleList = roleMapper.selectList(new QueryWrapper<SysRole>());
        for(SysRole role : roleList){
            ComboModel model = new ComboModel();
            model.setTitle(role.getRoleName());
            model.setId(role.getId());
            model.setRoleCode(role.getRoleCode());
            if(oConvertUtils.isNotEmpty(roleIds)) {
                for (int i = 0; i < roleIds.length; i++) {
                    if (roleIds[i].equals(role.getId())) {
                        model.setChecked(true);
                    }
                }
            }
            list.add(model);
        }
        return list;
    }

	@Override
	public List<String> getRoleIdsByUsername(String username) {
		return sysUserRoleMapper.getRoleIdByUserName(username);
	}

	@Override
	public String getDepartIdsByOrgCode(String orgCode) {
		return departMapper.queryDepartIdByOrgCode(orgCode);
	}

	@Override
	public List<SysDepartModel> getAllSysDepart() {
		List<SysDepartModel> departModelList = new ArrayList<SysDepartModel>();
		List<SysDepart> departList = departMapper.selectList(new QueryWrapper<SysDepart>().eq("del_flag","0"));
		for(SysDepart depart : departList){
			SysDepartModel model = new SysDepartModel();
			BeanUtils.copyProperties(depart,model);
			departModelList.add(model);
		}
		return departModelList;
	}

	@Override
	public DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId) {
		SysDataSource dbSource = dataSourceService.getById(dbSourceId);
		if(dbSource!=null && StringUtils.isNotBlank(dbSource.getDbPassword())){
			String dbPassword = dbSource.getDbPassword();
			String decodedStr = SecurityUtil.jiemi(dbPassword);
			dbSource.setDbPassword(decodedStr);
		}
		return new DynamicDataSourceModel(dbSource);
	}

	@Override
	public DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode) {
		SysDataSource dbSource = dataSourceService.getOne(new LambdaQueryWrapper<SysDataSource>().eq(SysDataSource::getCode, dbSourceCode));
		if(dbSource!=null && StringUtils.isNotBlank(dbSource.getDbPassword())){
			String dbPassword = dbSource.getDbPassword();
			String decodedStr = SecurityUtil.jiemi(dbPassword);
			dbSource.setDbPassword(decodedStr);
		}
		return new DynamicDataSourceModel(dbSource);
	}

	@Override
	public List<String> getDeptHeadByDepId(String deptId) {
		log.debug(" getDeptHeadByDepId Theo ID phòng ban, lấy người phụ trách, deptId:{}", deptId);
		if(oConvertUtils.isEmpty(deptId)){
			return null;
		}
		
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status", 1).eq("del_flag", 0);

		// Hỗ trợ truyền nhiều phòng ban, phân tách bằng dấu phẩy id
		if (oConvertUtils.isNotEmpty(deptId) && deptId.contains(SymbolConstant.COMMA)) {
			String[] vals = deptId.split(SymbolConstant.COMMA);

			// Trước tiên trim để loại bỏ khoảng trắng, sau đó lọc chuỗi rỗng, cuối cùng khử trùng lặp
			List<String> validDeptIds = Arrays.stream(vals)
					.map(String::trim)
					.filter(oConvertUtils::isNotEmpty)
					.distinct()  // Xử lý loại bỏ trùng lặp
					.collect(Collectors.toList());

			if (!validDeptIds.isEmpty()) {
				queryWrapper.and(andWrapper -> {
					for (int i = 0; i < validDeptIds.size(); i++) {
						andWrapper.like("depart_ids", validDeptIds.get(i));
						if (i < validDeptIds.size() - 1) {
							andWrapper.or();
						}
					}
				});
			}
		} else if (oConvertUtils.isNotEmpty(deptId)) {
			queryWrapper.like("depart_ids", deptId.trim());  // Giá trị đơn lẻ cũng cần trim
		}

		List<SysUser> userList = userMapper.selectList(queryWrapper);

		// Cũng xử lý khử trùng lặp cho kết quả
		return userList.stream()
				.map(SysUser::getUsername)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public void sendWebSocketMsg(String[] userIds, String cmd) {
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, cmd);
		webSocket.sendMessage(userIds, obj.toJSONString());
	}

	@Override
	public List<UserAccountInfo> queryAllUserByIds(String[] userIds) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		queryWrapper.in("id",userIds);
		List<UserAccountInfo> loginUsers = new ArrayList<>();
		List<SysUser> sysUsers = userMapper.selectList(queryWrapper);
		for (SysUser user:sysUsers) {
			UserAccountInfo loginUser=new UserAccountInfo();
			BeanUtils.copyProperties(user, loginUser);
			loginUsers.add(loginUser);
		}
		return loginUsers;
	}

	/**
	 * Đẩy thông tin nhân viên điểm danh
	 * @param userId
	 */
	@Override
	public void meetingSignWebsocket(String userId) {
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_SIGN);
		obj.put(WebsocketConst.MSG_USER_ID,userId);
		//TODO Hiện tại đẩy toàn bộ, sửa sau
		webSocket.sendMessage(obj.toJSONString());
	}

	@Override
	public List<UserAccountInfo> queryUserByNames(String[] userNames) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		queryWrapper.in("username",userNames);
		List<UserAccountInfo> loginUsers = new ArrayList<>();
		List<SysUser> sysUsers = userMapper.selectList(queryWrapper);
		for (SysUser user:sysUsers) {
			UserAccountInfo loginUser=new UserAccountInfo();
			BeanUtils.copyProperties(user, loginUser);
			loginUsers.add(loginUser);
		}
		return loginUsers;
	}

	@Override
	public List<JSONObject> queryUserBySuperQuery(String superQuery,String matchType) {
		List<JSONObject> result=new ArrayList<>();
		Map<String, String[]> parameterMap=new HashMap<>();
		parameterMap.put("superQueryMatchType",new String[]{matchType});
		parameterMap.put("superQueryParams",new String[]{superQuery});
		SysUser sysUser=new SysUser();
		QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, parameterMap);
		List<SysUser> list= sysUserService.list(queryWrapper);
		if(ObjectUtils.isNotEmpty(list)){
		
			// Giải thích logic mã: QQYUN-5326【Luồng đơn giản】Lấy nhân sự tổ chức, đơn/Nhiều điều kiện lọc, không có lọc theo phòng ban
			String departKey = "depart";
			QueryCondition departCondition = null;
			try {
				String temp = URLDecoder.decode(superQuery, "UTF-8");
				List<QueryCondition> conditions = JSON.parseArray(temp, QueryCondition.class);
				for(QueryCondition condition: conditions){
					if(departKey.equals(condition.getField())){
						departCondition = condition;
						break;
					}
				}
			} catch (UnsupportedEncodingException e) {
				log.error("Truy vấn thông tin người dùng, chuyển đổi JSON điều kiện truy vấn thất bại", e);
			}
			
			for (SysUser user : list) {
				JSONObject userJson = JSONObject.parseObject(JSONObject.toJSONString(user));
				List<SysDepart> departList = sysDepartService.queryDepartsByUsername(user.getUsername());
				List<String> departIds = null;
				if(departList!=null && departList.size()>0){
					departIds = departList.stream().map(i->i.getId()).collect(Collectors.toList());
					List<String> departNames = departList.stream().map(i->i.getDepartName()).collect(Collectors.toList());
					userJson.put(departKey, oConvertUtils.list2JSONArray(departIds));
					userJson.put(departKey+"_dictText", String.join(",", departNames));
				}
				boolean flag = getDepartConditionResult(departCondition, departIds);
				if(flag){
					result.add(userJson);
				}
				
			}
		}
		return result;
	}

	/**
	 * Xác định phòng ban của người dùng có thỏa mãn điều kiện hay không -Bằng/Không bằng/trong danh sách/Không ở--Trung bình/Trống/Không trống
	 * QQYUN-5326【Luồng đơn giản】Lấy nhân sự tổ chức, đơn/Nhiều điều kiện lọc, không có lọc theo phòng ban
	 * @param departCondition
	 * @param departIds
	 * @return
	 */
	private boolean getDepartConditionResult(QueryCondition departCondition, List<String> departIds){
		if(departCondition == null){
			return true;
		}
		QueryRuleEnum rule = QueryRuleEnum.getByValue(departCondition.getRule());
		String conditionVal = departCondition.getVal();
		if(rule == QueryRuleEnum.EMPTY){
			if(departIds==null || departIds.size()==0){
				return true;
			}
		}else if (rule == QueryRuleEnum.NOT_EMPTY){
			if(departIds!=null && departIds.size()>0){
				return true;
			}
		}else{
			if(oConvertUtils.isEmpty(conditionVal)){
				return false;
			}
			if(departIds==null || departIds.size()==0){
				return false;
			}
			List<String> conditionList;
			if(conditionVal.startsWith("[") && conditionVal.endsWith("]")){
				conditionList = JSONArray.parseArray(conditionVal, String.class);
			}else{
				conditionList = new ArrayList<String>(Arrays.asList(conditionVal.split(",")));
			}
			if(rule == QueryRuleEnum.EQ){
				if(oConvertUtils.isEqList(conditionList, departIds)){
					return true;
				}
			}else if(rule == QueryRuleEnum.NE){
				if(!oConvertUtils.isEqList(conditionList, departIds)){
					return true;
				}
			}else if(rule == QueryRuleEnum.IN){
				if(oConvertUtils.isInList(departIds, conditionList)){
					return true;
				}
			}else if(rule == QueryRuleEnum.NOT_IN){
				if(!oConvertUtils.isInList(departIds, conditionList)){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public JSONObject queryUserById(String id) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(true, SysUser::getId, id);
		SysUser sysUser = sysUserService.getOne(queryWrapper);
		if (ObjectUtils.isNotEmpty(sysUser)) {
			return JSONObject.parseObject(JSONObject.toJSONString(sysUser));
		}
		return null;
	}

	@Override
	public List<JSONObject> queryDeptBySuperQuery(String superQuery,String matchType) {
		List<JSONObject> result=new ArrayList<>();
		Map<String, String[]> parameterMap=new HashMap<>();
		parameterMap.put("superQueryMatchType",new String[]{matchType});
		parameterMap.put("superQueryParams",new String[]{superQuery});
		SysDepart sysDepart=new SysDepart();
		QueryWrapper<SysDepart> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, parameterMap);
		List<SysDepart> list= sysDepartService.list(queryWrapper);
		if(ObjectUtils.isNotEmpty(list)){
			for (SysDepart depart: list) {
				result.add(JSONObject.parseObject(JSONObject.toJSONString(depart)));
			}
		}
		return result;
	}

	@Override
	public List<JSONObject> queryRoleBySuperQuery(String superQuery,String matchType) {
		List<JSONObject> result=new ArrayList<>();
		Map<String, String[]> parameterMap=new HashMap<>();
		parameterMap.put("superQueryMatchType",new String[]{matchType});
		parameterMap.put("superQueryParams",new String[]{superQuery});
		SysRole sysDepart=new SysRole();
		QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, parameterMap);
		List<SysRole> list= sysRoleService.list(queryWrapper);
		if(ObjectUtils.isNotEmpty(list)){
			for (SysRole role: list) {
				result.add(JSONObject.parseObject(JSONObject.toJSONString(role)));
			}
		}
		return result;
	}

	@Override
	public List<String> selectUserIdByTenantId(String tenantId) {
		QueryWrapper queryWrapper=new QueryWrapper<SysUserTenant>();
		queryWrapper.select("user_id");
		queryWrapper.eq("tenant_id",tenantId);
		return sysUserTenantService.listObjs(queryWrapper,e->e.toString());
	}

	@Override
	public SysDepartModel selectAllById(String id) {
		SysDepart sysDepart = sysDepartService.getById(id);
		SysDepartModel sysDepartModel = new SysDepartModel();
		BeanUtils.copyProperties(sysDepart,sysDepartModel);
		return sysDepartModel;
	}

	@Override
	public List<String> queryDeptUsersByUserId(String userId) {
		List<String> userIds = new ArrayList<>();
		List<SysUserDepart> userDepartList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().eq("user_id",userId));
		if(userDepartList != null){
			//Tìm công ty trực thuộc
			String orgCodes = "";
			StringBuilder orgCodesBuilder = new StringBuilder();
            orgCodesBuilder.append(orgCodes);
			for(SysUserDepart userDepart : userDepartList){
				//Truy vấn mã công ty trực thuộc
				SysDepart depart = sysDepartService.getById(userDepart.getDepId());
				int length = YouBianCodeUtil.ZHANWEI_LENGTH;
				String compyOrgCode = "";
				if(depart != null && depart.getOrgCode() != null){
					compyOrgCode = depart.getOrgCode().substring(0,length);
					if(orgCodes.indexOf(compyOrgCode) == -1){
                        orgCodesBuilder.append(SymbolConstant.COMMA).append(compyOrgCode);
					}
				}
			}
            orgCodes = orgCodesBuilder.toString();
			if(oConvertUtils.isNotEmpty(orgCodes)){
				orgCodes = orgCodes.substring(1);
				List<String> listIds = departMapper.getSubDepIdsByOrgCodes(orgCodes.split(","));
				List<SysUserDepart> userList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().in("dep_id",listIds));
				for(SysUserDepart userDepart : userList){
					if(!userIds.contains(userDepart.getUserId())){
						userIds.add(userDepart.getUserId());
					}
				}
			}
		}
		return userIds;
	}

	/**
	 * Truy vấn tập hợp vai trò mà người dùng sở hữu
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> getUserRoleSet(String username) {
		// Truy vấn tập hợp vai trò mà người dùng sở hữu
		List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
		log.debug("-------Đọc các vai trò mà người dùng sở hữu qua cơ sở dữ liệu Rules------username： " + username + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}
	
	
	/**
	 * Truy vấn tập hợp vai trò mà người dùng sở hữu
	 * @param useId
	 * @return
	 */
	@Override
	public Set<String> getUserRoleSetById(String useId) {
		// Truy vấn tập hợp vai trò mà người dùng sở hữu
		List<String> roles = sysUserRoleMapper.getRoleCodeByUserId(useId);
		log.debug("-------Đọc các vai trò mà người dùng sở hữu qua cơ sở dữ liệu Rules------useId： " + useId + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}

	/**
	 * Truy vấn tập hợp quyền hạn mà người dùng sở hữu
	 * @param userId
	 * @return
	 */
	@Override
	public Set<String> getUserPermissionSet(String userId) {
		Set<String> permissionSet = new HashSet<>();
		List<SysPermission> permissionList = sysPermissionMapper.queryByUser(userId);
		//================= begin Khi bật thuê bao, nếu không có vai trò test thì mặc định thêm vai trò test================
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			if (permissionList == null) {
				permissionList = new ArrayList<>();
			}
			List<SysPermission> testRoleList = sysPermissionMapper.queryPermissionByTestRoleId();
			permissionList.addAll(testRoleList);
		}
		//================= end Khi bật thuê bao, nếu không có vai trò test thì mặc định thêm vai trò test================
		for (SysPermission po : permissionList) {
//			// TODO URL Quy tắc có vấn đề?
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(po.getPerms())) {
				permissionSet.add(po.getPerms());
			}
		}
		log.debug("-------Đọc các quyền hạn mà người dùng sở hữu qua cơ sở dữ liệu Perms------userId： "+ userId+",Perms size: "+ (permissionSet==null?0:permissionSet.size()) );
		return permissionSet;
	}

	/**
	 * Kiểm tra menu online có quyền hay không
	 * @param onlineAuthDTO
	 * @return
	 */
	@Override
	public boolean hasOnlineAuth(OnlineAuthDTO onlineAuthDTO) {
		String username = onlineAuthDTO.getUsername();
		List<String> possibleUrl = onlineAuthDTO.getPossibleUrl();
		String onlineFormUrl = onlineAuthDTO.getOnlineFormUrl();
		//Truy vấn menu
		LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
		query.eq(SysPermission::getDelFlag, 0);
		query.in(SysPermission::getUrl, possibleUrl);
		List<SysPermission> permissionList = sysPermissionMapper.selectList(query);
		if (permissionList == null || permissionList.size() == 0) {
			//Chưa cấu hình menu thì tìm địa chỉ menu biểu mẫu online
			SysPermission sysPermission = new SysPermission();
			sysPermission.setUrl(onlineFormUrl);
			int count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
			if(count<=0){
				// Giải thích logic mã: [QQYUN-7992][online] Form online thuộc đơn công việc, chưa cấu hình menu phát triển form online, thao tác báo lỗi không có quyền------------
				sysPermission.setUrl(onlineAuthDTO.getOnlineWorkOrderUrl());
				count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
				if(count<=0) {
					return false;
				}
			}
		} else {
			//Đã tìm thấy menu
			boolean has = false;
			for (SysPermission p : permissionList) {
				int count = sysPermissionMapper.queryCountByUsername(username, p);
				has = has || (count>0);
			}
			if (!has) {
				//Chưa cấu hình menu thì tìm địa chỉ menu biểu mẫu online
				SysPermission sysPermission = new SysPermission();
				sysPermission.setUrl(onlineFormUrl);
				int count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
				if (count <= 0) {
					// Giải thích logic mã: [QQYUN-7992][online] Form online thuộc đơn công việc, chưa cấu hình menu phát triển form online, thao tác báo lỗi không có quyền------------
					sysPermission.setUrl(onlineAuthDTO.getOnlineWorkOrderUrl());
					count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
					if (count > 0) {
						has = true;
					}
				} else {
					has = true;
				}
			}
			return has;
		}
		return true;
	}

	/**
	 * Truy vấn tập hợp vai trò mà người dùng sở hữu — triển khai interface trong common api
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> queryUserRoles(String username) {
		return getUserRoleSet(username);
	}

	@Override
	public Set<String> queryUserRolesById(String userId) {
		return getUserRoleSetById(userId);
	}

	/**
	 * Truy vấn tập hợp quyền hạn mà người dùng sở hữu — triển khai interface trong common api
	 * @param userId
	 * @return
	 */
	@Override
	public Set<String> queryUserAuths(String userId) {
		return getUserPermissionSet(userId);
	}

	/**
	 * 36 Theo nhiều tài khoản người dùng(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
	 * @param usernames
	 * @return
	 */
	@Override
	public List<JSONObject> queryUsersByUsernames(String usernames) {
		LambdaQueryWrapper<SysUser> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysUser::getUsername,usernames.split(","));
		return JSON.parseArray(JSON.toJSONString(userMapper.selectList(queryWrapper))).toJavaList(JSONObject.class);
	}

	@Override
	public List<JSONObject> queryUsersByIds(String ids) {
		LambdaQueryWrapper<SysUser> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysUser::getId,ids.split(","));
		return JSON.parseArray(JSON.toJSONString(userMapper.selectList(queryWrapper))).toJavaList(JSONObject.class);
	}

	/**
	 * 37 Theo nhiều mã phòng ban(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
	 * @param orgCodes
	 * @return
	 */
	@Override
	public List<JSONObject> queryDepartsByOrgcodes(String orgCodes) {
		LambdaQueryWrapper<SysDepart> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysDepart::getOrgCode,orgCodes.split(","));
		return JSON.parseArray(JSON.toJSONString(sysDepartService.list(queryWrapper))).toJavaList(JSONObject.class);
	}

	@Override
	public List<JSONObject> queryDepartsByIds(String ids) {
		LambdaQueryWrapper<SysDepart> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysDepart::getId,ids.split(","));
		return JSON.parseArray(JSON.toJSONString(sysDepartService.list(queryWrapper))).toJavaList(JSONObject.class);
	}

	/**
     * Gửi tin nhắn
     *
     * @param fromUser
     * @param toUser
     * @param title
     * @param msgContent
     * @param setMsgCategory
     * @param noticeType
     */
	private void sendSysAnnouncement(String fromUser, String toUser, String title, String msgContent, String setMsgCategory, String noticeType) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(setMsgCategory);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		// Giải thích logic mã: [QQYUN-12999]Thông báo hệ thống, thời gian thông báo hệ thống được cập nhật nhưng lại bị xếp xuống dưới
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		if(oConvertUtils.isEmpty(noticeType)){
            noticeType = NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue();
        }
        announcement.setNoticeType(noticeType);
		sysAnnouncementMapper.insert(announcement);
		// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				obj.put(CommonConstant.NOTICE_TYPE, noticeType);
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}

	}

	/**
	 * Gửi tin nhắn, kèm tham số nghiệp vụ
	 * @param fromUser
	 * @param toUser
	 * @param title
	 * @param msgContent
	 * @param setMsgCategory
	 * @param busType
	 * @param busId
	 * @param noticeType Loại tin nhắn,for [JHHB-136]【vue3】Tin nhắn hệ thống làm việc cộng tác cần thêm một loại
	 */
	private void sendBusAnnouncement(String fromUser, String toUser, String title, String msgContent, String setMsgCategory, String busType, String busId, String noticeType) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(setMsgCategory);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		announcement.setBusId(busId);
		announcement.setBusType(busType);

		// Giải thích logic mã: busType khi trống, báo lỗi
		if(oConvertUtils.isNotEmpty(busType)){
			announcement.setOpenType(SysAnnmentTypeEnum.getByType(busType).getOpenType());
			announcement.setOpenPage(SysAnnmentTypeEnum.getByType(busType).getOpenPage());
		}

		// Giải thích logic mã: [JHHB-136]【vue3】Tin nhắn hệ thống làm việc cộng tác cần thêm một loại------------
		if(oConvertUtils.isEmpty(noticeType)){
			noticeType = NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue();
		}
		announcement.setNoticeType(noticeType);
		// Giải thích logic mã: [QQYUN-12999]Thông báo hệ thống, thời gian thông báo hệ thống được cập nhật nhưng lại bị xếp xuống dưới
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		sysAnnouncementMapper.insert(announcement);
		// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				obj.put(CommonConstant.NOTICE_TYPE, NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue());
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}
	}

	/**
	 * Gửi tin nhắn email
	 * @param email
	 * @param title
	 * @param content
	 */
	@Override
	public void sendEmailMsg(String email, String title, String content) {
			EmailSendMsgHandle emailHandle=new EmailSendMsgHandle();
			emailHandle.sendMsg(email, title, content);
	}
	/**
	 * Gửi tin nhắn SMS
	 * @param phone Số điện thoại
	 * @param param Tham số mẫu
	 * @param dySmsEnum Mẫu SMS
	 */
	@Override
	public void sendSmsMsg(String phone, JSONObject param,DySmsEnum dySmsEnum) {
        try {
			log.info(" Gửi tin nhắn SMS phone = {}", phone);
			log.info(" Gửi tin nhắn SMS param = {}", param);
            DySmsHelper.sendSms(phone, param,dySmsEnum);
        } catch (ClientException e) {
			e.printStackTrace();
        }
    }

	/**
	 * Gửi tin nhắn email mẫu html
	 * @param email
	 * @param title
	 * @param emailTemplateEnum
	 * @param params
	 */
	@Override
	public void sendHtmlTemplateEmail(String email, String title, EmailTemplateEnum emailTemplateEnum,JSONObject params) {
		EmailSendMsgHandle emailHandle=new EmailSendMsgHandle();
		String htmlText="";
		try{
			//Lấy thể hiện mẫu
			Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
			//Thiết lập thư mục file mẫu
			String url = emailTemplateEnum.getUrl();
			configuration.setClassForTemplateLoading(this.getClass(), url.substring(0,url.lastIndexOf("/")));
			configuration.setDefaultEncoding("UTF-8");
			//Cài đặt báo lỗi giá trị trống
			configuration.setClassicCompatible(true);
			configuration.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
			Template template = configuration.getTemplate(url.substring(url.lastIndexOf("/")));
			//Phân tích tệp mẫu
			htmlText= FreeMarkerTemplateUtils.processTemplateIntoString(template, params);
		}catch(IOException e){
			e.printStackTrace();
		}catch(TemplateException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

		log.debug("Email Html Text：{}", htmlText);
		emailHandle.sendMsg(email, title, htmlText);
	}

	/**
	 * Lấy phòng ban cấp dưới của công ty và thông tin id của tất cả người dùng
	 * @param orgCode
	 * @return
	 */
	@Override
	public List<Map> getDeptUserByOrgCode(String orgCode) {
		//1.Lấy thông tin công ty
		SysDepart comp=sysDepartService.queryCompByOrgCode(orgCode);
		if(comp!=null){
			//2.Lấy phòng ban cấp dưới của công ty
			List<SysDepart> departs=sysDepartService.queryDeptByPid(comp.getId());
			//3.Lấy thông tin nhân sự thuộc phòng ban
			 List<Map> list=new ArrayList();
			 //4.Xử lý dữ liệu phòng ban và người dùng cấp dưới
			for (SysDepart dept:departs) {
				Map map=new HashMap(5);
				//Tên phòng ban
				String departName = dept.getDepartName();
				//Lấy các phòng ban cấp dưới theo mã phòng ban id
				List<String> listIds = departMapper.getSubDepIdsByDepId(dept.getId());
				//Lấy tất cả người dùng của phòng ban cấp dưới theo ids phòng ban cấp dưới
				List<SysUserDepart> userList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().in("dep_id",listIds));
				List<String> userIds = new ArrayList<>();
				for(SysUserDepart userDepart : userList){
					if(!userIds.contains(userDepart.getUserId())){
						userIds.add(userDepart.getUserId());
					}
				}
				map.put("name",departName);
				map.put("ids",userIds);
				list.add(map);
			}
			return list;
		}
		return null;
	}

	/**
	 * Truy vấn bản dịch danh mục phân loại
	 *
	 * @param ids Bảng danh mục phân loại id
	 * @return
	 */
	@Override
	public List<String> loadCategoryDictItem(String ids) {
		return sysCategoryService.loadDictItem(ids, false);
	}

	@Override
	public List<String> loadCategoryDictItemByNames(String names, boolean delNotExist) {
		return sysCategoryService.loadDictItemByNames(names, delNotExist);
	}

	/**
	 * Tải danh mục theo code danh mục text
	 *
	 * @param dictCode Thứ tự: tableName,text,code
	 * @param keys     cần truy vấn key
	 * @return
	 */
	@Override
	public List<String> loadDictItem(String dictCode, String keys) {
		String[] params = dictCode.split(",");
		return sysDictService.queryTableDictByKeys(params[0], params[1], params[2], keys, false);
	}

	@Override
	public Map<String, String> copyLowAppDict(String originalAppId, String appId, String tenantId) {
		Map<String, String> dictCodeMapping = new HashMap<String, String>();
		List<SysDictVo> ls = sysDictService.getDictListByLowAppId(originalAppId);
		for (SysDictVo vo : ls) {
			vo.setId(null);
			vo.setLowAppId(appId);
			vo.setTenantId(oConvertUtils.getInt(tenantId, null));
			String newDictCode = sysDictService.addDictByLowAppId(vo);
			dictCodeMapping.put(vo.getDictCode(), newDictCode);
		}

		log.info(" --- Sao chép hàng loạt danh mục dưới ứng dụng sang tenant mới —— Ứng dụng gốc ID:{}，ứng dụng mới ID:{}, ID tenant mới:{}，số lượng danh mục：{} --- ", originalAppId, appId, tenantId, dictCodeMapping.size());
		return dictCodeMapping;
	}

	/**
	 * Truy vấn mục từ điển theo code từ điển
	 *
	 * @param dictCode Thứ tự: tableName,text,code
	 * @param dictCode cần truy vấn key
	 * @return
	 */
	@Override
	public List<DictModel> getDictItems(String dictCode) {
		List<DictModel> ls = sysDictService.getDictItems(dictCode);
		if (ls == null) {
			ls = new ArrayList<>();
		}
		return ls;
	}

	/**
	 * Theo nhiều code danh mục, truy vấn nhiều mục danh mục
	 *
	 * @param dictCodeList
	 * @return key = dictCode ； value=Mục danh mục tương ứng
	 */
	@Override
	public Map<String, List<DictModel>> getManyDictItems(List<String> dictCodeList) {
		return sysDictService.queryDictItemsByCodeList(dictCodeList);
	}

	/**
	 * 【Tìm kiếm thả xuống】
	 * Bảng từ điển có lượng dữ liệu lớn sẽ tải bất đồng bộ, tức là frontend nhập nội dung để lọc dữ liệu
	 *
	 * @param dictCode Định dạng code từ điển:table,text,code
	 * @param keyword  Lọc từ khóa
	 * @return
	 */
	@Override
	public List<DictModel> loadDictItemByKeyword(String dictCode, String keyword, Integer pageNo, Integer pageSize) {
		return sysDictService.loadDict(dictCode, keyword,pageNo, pageSize);
	}

	@Override
	public Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys) {
		List<String> dictCodeList = Arrays.asList(dictCodes.split(","));
		List<String> values = Arrays.asList(keys.split(","));
		// Giải thích logic mã: [issues/#5643]Giải quyết vấn đề danh mục bảng liên cơ sở dữ liệu không truy vấn được trong môi trường phân tán------------
		return sysDictService.queryManyDictByKeys(dictCodeList, values);
	}

	// Giải thích logic mã: [issues/#5643]Giải quyết vấn đề danh mục bảng liên cơ sở dữ liệu không truy vấn được trong môi trường phân tán------------
	@Override
	public List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys, String dataSource) {
		return sysDictService.queryTableDictTextByKeys(table, text, code, Arrays.asList(keys.split(",")), dataSource);
	}

	@Override
	public List<DictModel> queryPopupDictItems(String reportCode, String dictCode, String dictText, String dataList) {
		try {
			return onlineBaseExtApi.cgreportGetDataPackage(reportCode, dictText, dictCode, dataList);
		} catch (Exception e) {
			log.warn("Tải danh mục dữ liệu thất bại, reportCode: {}", reportCode, e);
			return Collections.emptyList();
		}
	}

	//-------------------------------------Nút quy trình gửi tin nhắn mẫu-----------------------------------------------
	@Autowired
	private QywxSendMsgHandle qywxSendMsgHandle;

	@Autowired
	private SystemSendMsgHandle systemSendMsgHandle;

	@Autowired
	private EmailSendMsgHandle emailSendMsgHandle;

	@Autowired
	private DdSendMsgHandle ddSendMsgHandle;
	@Autowired
	private FsSendMsgHandle fsSendMsgHandle;
	@Autowired
	private SmsSendMsgHandle smsSendMsgHandle;

	@Override
	public void sendTemplateMessage(MessageDTO message) {
		String messageType = message.getType();
		log.debug(" 【Chung đa năng】Đẩy tin nhắn messageType = {}", messageType);
		// Giải thích logic mã: Chuyển mã phân tích mẫu sang khâu gửi tin nhắn, Mà không phải nơi gọi
		String templateCode = message.getTemplateCode();
		if(oConvertUtils.isNotEmpty(templateCode)){
			SysMessageTemplate templateEntity = getTemplateEntity(templateCode);
			boolean isMarkdown = CommonConstant.MSG_TEMPLATE_TYPE_MD.equals(templateEntity.getTemplateType());
			String content = templateEntity.getTemplateContent();
			if(oConvertUtils.isNotEmpty(content) && null!=message.getData()){
				content = FreemarkerParseFactory.parseTemplateContent(content, message.getData(), isMarkdown);
			}
			message.setIsMarkdown(isMarkdown);
			message.setContent(content);
		}
		if(oConvertUtils.isEmpty(message.getContent())){
			log.error("Gửi tin nhắn thất bại,Nội dung tin nhắn trống!");
			throw new JeecgBootException("Gửi tin nhắn thất bại,Nội dung tin nhắn trống!");
		}

		if(MessageTypeEnum.XT.getType().equals(messageType)){
			if (message.isMarkdown()) {
				// Tin nhắn hệ thống cần phân tích cú pháp Markdown
				message.setContent(HTMLUtils.parseMarkdown(message.getContent()));
			}
			systemSendMsgHandle.sendMessage(message);
		}else if(MessageTypeEnum.YJ.getType().equals(messageType)){
			if (message.isMarkdown()) {
				// Tin nhắn email cần phân tích cú pháp Markdown
				message.setContent(HTMLUtils.parseMarkdown(message.getContent()));
			}
			// Giải thích logic mã: 【QQYUN-8523】QiaoQiaoYun gửi thông báo email, không ổn định---
			if(message.getIsTimeJob() != null && message.getIsTimeJob()){
				emailSendMsgHandle.sendEmailMessage(message);
			}else{
				emailSendMsgHandle.sendMessage(message);
			}
		}else if(MessageTypeEnum.DD.getType().equals(messageType)){
			executeWithTenant(() -> ddSendMsgHandle.sendMessage(message));
		}else if(MessageTypeEnum.QYWX.getType().equals(messageType)){
			executeWithTenant(() -> qywxSendMsgHandle.sendMessage(message));
		}else if(MessageTypeEnum.FS.getType().equals(messageType)){
			executeWithTenant(() -> fsSendMsgHandle.sendMessage(message));
		}else if(MessageTypeEnum.DX.getType().equals(messageType)){
			executeWithTenant(() -> smsSendMsgHandle.sendMessage(message));
		}
	}

	//update-begin---author:liusq ---date:2026-10-19  for：【QQYUN-12767】DingTalk/WeCom/Feishu/Khi gửi SMS bất đồng bộ thì truyền ID tenant, sửa lỗi tin nhắn quy trình lấy được tenant 0 trong thread pool-----------
	/**
	 * Gửi vào pool luồng bất đồng bộ của tin nhắn, đồng thời truyền ID thuê bao của luồng hiện tại sang luồng bất đồng bộ.
	 * <p>TenantContext là ThreadLocal thông thường, sau khi chuyển sang pool luồng sẽ không lấy được giá trị, DingTalk/WeCom/Feishu sẽ truy vấn cấu hình ứng dụng và tài khoản gắn kết bên thứ ba theo tenant 0, khiến tin nhắn không gửi được.</p>
	 *
	 * @param task Gửi tác vụ
	 */
	private void executeWithTenant(Runnable task) {
		String tenantId = TenantContext.getTenant();
		MSG_THREAD_POOL.execute(() -> {
			TenantContext.setTenant(tenantId);
			try {
				task.run();
			} finally {
				// Luồng trong pool sẽ được tái sử dụng, bắt buộc phải dọn sạch để tránh các tác vụ sau bị lẫn thuê bao
				TenantContext.clear();
			}
		});
	}
	//update-end---author:liusq ---date:2026-10-19  for：【QQYUN-12767】DingTalk/WeCom/Feishu/Khi gửi SMS bất đồng bộ thì truyền ID tenant, sửa lỗi tin nhắn quy trình lấy được tenant 0 trong thread pool-----------

	@Override
	public String getTemplateContent(String code) {
		List<SysMessageTemplate> list = sysMessageTemplateService.selectByCode(code);
		if(list==null || list.size()==0){
			return null;
		}
		return list.get(0).getTemplateContent();
	}

	/**
	 * Lấy nội dung mẫu, phân tích markdown
	 *
	 * @param code
	 * @return
	 */
	public SysMessageTemplate getTemplateEntity(String code) {
		List<SysMessageTemplate> list = sysMessageTemplateService.selectByCode(code);
		if (list == null || list.size() == 0) {
			return null;
		}
		return list.get(0);
	}

	//-------------------------------------Nút quy trình gửi tin nhắn mẫu-----------------------------------------------

	@Override
	public void saveDataLog(DataLogDTO dataLogDto) {
		try {
			SysDataLog entity = new SysDataLog();
			entity.setDataTable(dataLogDto.getTableName());
			entity.setDataId(dataLogDto.getDataId());
			entity.setDataContent(dataLogDto.getContent());
			entity.setType(dataLogDto.getType());
			entity.setDataVersion("1");
			if (oConvertUtils.isNotEmpty(dataLogDto.getCreateBy())) {
				entity.setCreateBy(dataLogDto.getCreateBy());
			}
			if (oConvertUtils.isNotEmpty(dataLogDto.getCreateName())) {
				entity.setCreateName(dataLogDto.getCreateName());
			} else {
				entity.autoSetCreateName();
			}
			sysDataLogService.save(entity);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			//e.printStackTrace();
		}
	}

    @Override
    public void updateAvatar(LoginUser loginUser) {
		SysUser sysUser = new SysUser();
		// Tạo đối tượng UpdateWrapper
		UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", loginUser.getId()); // Đặt điều kiện cập nhật
		sysUser.setAvatar(loginUser.getAvatar()); // Đặt trường cần cập nhật
		sysUserService.update(sysUser, updateWrapper);
    }

	@Override
	public void sendAppChatSocket(String userId) {
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.MSG_CHAT);
		obj.put(WebsocketConst.MSG_USER_ID, userId);
		webSocket.sendMessage(userId, obj.toJSONString());
	}

	@Override
	public String getRoleCodeById(String id) {
		SysRole role = roleMapper.selectById(id);
		if(role!=null){
			return role.getRoleCode();
		}
		return null;
	}

	@Override
	public List<DictModel> queryRoleDictByCode(String roleCodes) {
		if (oConvertUtils.isEmpty(roleCodes)) {
			return new ArrayList<>();
		}
		List<String> codeList = Arrays.asList(roleCodes.split(","));
		LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(SysRole::getRoleCode, codeList);
		List<SysRole> list = roleMapper.selectList(queryWrapper);
		// Chuyển đổi thành SysRoleVo
		return list.stream().map(sysRole -> {
			DictModel model = new DictModel();
			model.setText(sysRole.getRoleName());
			model.setValue(sysRole.getRoleCode());
			return model;
		}).collect(Collectors.toList());
	}

	@Override
	public List<String> queryUserIdsByDeptIds(List<String> deptIds) {
		QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().select(SysUserDepart::getUserId).in(true,SysUserDepart::getDepId,deptIds);
		List<String> userIds = sysUserDepartService.listObjs(queryWrapper,e->e.toString());
		//update-begin---author:zhangdaiscott ---date:2026-07-16  for：【LHZP-789】Lọc sys_user_depart Người dùng trong bảng quan hệ đã bị xóa/Dữ liệu rác của người dùng đã bị xóa/vô hiệu hóa-----------
		if (userIds == null || userIds.isEmpty()) {
			return userIds;
		}
		QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.lambda().select(SysUser::getId).eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1))
				.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0).in(SysUser::getId,userIds);
		return sysUserService.listObjs(userQueryWrapper,e->e.toString());
		//update-end---author:zhangdaiscott ---date:2026-07-16  for：【LHZP-789】Lọc sys_user_depart Người dùng trong bảng quan hệ đã bị xóa/Dữ liệu rác của người dùng đã bị xóa/vô hiệu hóa-----------
	}

	@Override
	public List<String> queryUsernameByIds(List<String> userIds) {
		return userMapper.getUsernameByIds(userIds);
	}

    @Override
    public List<String> queryUserIdsByCascadeDeptIds(List<String> deptIds) {
        Set<String> userIds = new HashSet<>();
        List<SysDepart> departs = sysDepartService.list(Wrappers.lambdaQuery(SysDepart.class)
                .select(SysDepart::getOrgCode)
                .in(SysDepart::getId, deptIds));
        departs.forEach(depart -> {
            List<SysUser> sysUsers = sysUserDepartService.queryUserByDepCode(depart.getOrgCode(), null, null);
            if(oConvertUtils.isObjectNotEmpty(sysUsers)){
                userIds.addAll(sysUsers.stream().map(SysUser::getId).collect(Collectors.toSet()));
            }
        });
        return new ArrayList<>(userIds);
    }

    @Override
	public List<String> queryUserAccountsByDeptIds(List<String> deptIds) {
		return departMapper.queryUserAccountByDepartIds(deptIds);
	}

	@Override
	public List<String> queryUserIdsByRoleds(List<String> roleCodes) {
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>()
				.in(SysRole::getRoleCode, roleCodes);
		List<SysRole> roleList = sysRoleService.list(query);
		if(roleList!=null && roleList.size()>0){
			List<String> idList = roleList.stream().map(role->role.getId()).collect(Collectors.toList());
			QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
			queryWrapper.lambda().select(SysUserRole::getUserId).in(true,SysUserRole::getRoleId, idList);
			List<String> userIds = sysUserRoleService.listObjs(queryWrapper,e->e.toString());
			//update-begin---author:zhangdaiscott ---date:2026-07-16  for：【LHZP-789】Lọc sys_user_role Người dùng trong bảng quan hệ đã bị xóa/Dữ liệu rác của người dùng đã bị xóa/vô hiệu hóa-----------
			if (userIds == null || userIds.isEmpty()) {
				return userIds;
			}
			QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
			userQueryWrapper.lambda().select(SysUser::getId).eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1))
					.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0).in(SysUser::getId,userIds);
			return sysUserService.listObjs(userQueryWrapper,e->e.toString());
			//update-end---author:zhangdaiscott ---date:2026-07-16  for：【LHZP-789】Lọc sys_user_role Người dùng trong bảng quan hệ đã bị xóa/Dữ liệu rác của người dùng đã bị xóa/vô hiệu hóa-----------
		}
		return null;
	}

	@Override
	public List<String> queryUserIdsByDeptPostIds(List<String> deptPostIds) {
		// 1.Truy vấn người dùng tương ứng với chức vụ kiêm nhiệm
		QueryWrapper<SysUserDepPost> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().select(SysUserDepPost::getUserId).in(true,SysUserDepPost::getDepId,deptPostIds);
		List<String> otherDepartPostUserIds = sysUserDepPostService.listObjs(queryWrapper,e->e.toString());
		log.info("Người dùng tương ứng với chức vụ kiêm nhiệm otherDepartPostUserIds = "+ JSON.toJSONString(otherDepartPostUserIds));

		// 2.Truy vấn người dùng tương ứng với chức vụ chính
		QueryWrapper<SysUser> mainQueryWrapper = new QueryWrapper<>();
		mainQueryWrapper.lambda().select(SysUser::getId).eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1)).eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0)
				.and(wrapper -> wrapper.in(SysUser::getMainDepPostId, deptPostIds));
		List<String> mainDepartPostUserIds =  sysUserService.listObjs(mainQueryWrapper,e->e.toString());
		log.info("Người dùng tương ứng với chức vụ chính mainDepartPostUserIds = "+ JSON.toJSONString(mainDepartPostUserIds));
		
		// 3.Gộp người dùng tương ứng với vị trí chính và vị trí kiêm nhiệm
		Set<String> userIdSet = new HashSet<>();
		if (otherDepartPostUserIds != null && !otherDepartPostUserIds.isEmpty()) {
			userIdSet.addAll(otherDepartPostUserIds);
		}
		if (mainDepartPostUserIds != null && !mainDepartPostUserIds.isEmpty()) {
			userIdSet.addAll(mainDepartPostUserIds);
		}
		log.info("Vị trí chính và vị trí kiêm nhiệm, người dùng tương ứng userIdSet = "+ JSON.toJSONString(userIdSet));
		return new ArrayList<>(userIdSet);
	}

	@Override
	public List<String> queryUsernameByDepartPositIds(List<String> deptPostIds) {
		// 1.Truy vấn người dùng tương ứng với chức vụ kiêm nhiệm
		QueryWrapper<SysUserDepPost> otherQueryWrapper = new QueryWrapper<>();
		otherQueryWrapper.lambda().select(SysUserDepPost::getUserId).in(true, SysUserDepPost::getDepId, deptPostIds);
		List<String> otherUserIds = sysUserDepPostService.listObjs(otherQueryWrapper, e -> e.toString());
		log.info("Người dùng tương ứng với chức vụ kiêm nhiệm otherUserIds = {}，size = {}" + JSON.toJSONString(otherUserIds), oConvertUtils.getCollectionSize(otherUserIds));

		// 2.Truy vấn người dùng tương ứng với vị trí chính và vị trí kiêm nhiệm
		QueryWrapper<SysUser> mainQueryWrapper = new QueryWrapper<>();
		mainQueryWrapper.lambda().select(SysUser::getUsername).eq(SysUser::getStatus, Integer.parseInt(CommonConstant.STATUS_1)).eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0)
				.and(wrapper -> wrapper
						.in(SysUser::getMainDepPostId, deptPostIds)
						.or()
						.in(otherUserIds != null && !otherUserIds.isEmpty(), SysUser::getId, otherUserIds)
				);
		List<String> allUsernames = sysUserService.listObjs(mainQueryWrapper, e -> e.toString());
		log.info("Vị trí chính và vị trí kiêm nhiệm, tài khoản người dùng tương ứng allUsernames = {}，size = {}" ,JSON.toJSONString(allUsernames), oConvertUtils.getCollectionSize(allUsernames));
		return allUsernames;
	}

	@Override
	public List<String> queryUserIdsByPositionIds(List<String> positionIds) {
		QueryWrapper<SysUserPosition> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().select(SysUserPosition::getUserId).in(true,SysUserPosition::getPositionId,positionIds);
		return sysUserPositionService.listObjs(queryWrapper,e->e.toString());
	}
	
	/**
	 * Lấy địa chỉ báo cáo có tham số; vì thứ tự của nhiều tham số có thể thay đổi nên cần sắp xếp lại thứ tự tham số để lấy tập hợp tất cả các địa chỉ có thể
	 * Như sau: điều chỉnh thứ tự tham số để dùng truy vấn in thì có thể truy vấn ra dữ liệu menu
	 *  /online/cgreport/1624393012494286850?name=1&age=16
	 *  /online/cgreport/1624393012494286850?age=16&name=1
	 * @param path
	 * @return
	 */
	private List<String> getOnlinePossiblePaths(String path){
		List<String> result = new ArrayList<>();
		log.debug(" path = "+ path);
		if (path.indexOf("?") >= 0 && (path.contains("/online/cgreport/") || path.contains("/online/cgformList/") || path.contains("/online/graphreport/"))) {
			//Chứa?Cho thấy có nhiều tham số
			String[] pathArray = path.split("\\?");
			if(oConvertUtils.isNotEmpty(pathArray[1])){
				String[] params = pathArray[1].split("&");
				if(params.length==1){
					result.add(path);
				}else{
					result = anm(pathArray[0], Arrays.asList(params));
				}
			}else{
				result.add(path);
			}
		}else{
			result.add(path);
		}
		return result;
	}


	/**
	 * Lấy các hoán vị khác nhau của các phần tử trong mảng a(n,m)
	 * @param list
	 * @return
	 */
	private List<String> anm(String baseUrl, List<String> list) {
		int len = list.size();
		int[] destArray = new int[len];
		for (int i = 0; i < len; i++) {
			destArray[i] = i;
		}
		int[] temp = new int[len];
		List<String> result = new ArrayList<>();
		while (temp[0] < len) {
			temp[temp.length - 1]++;
			for (int i = temp.length - 1; i > 0; i--) {
				if (temp[i] == len) {
					temp[i] = 0;
					temp[i - 1]++;
				}
			}
			int[] tt = temp.clone();
			Arrays.sort(tt);
			if (!Arrays.equals(tt, destArray)) {
				continue;
			}
			String str = "";
			for (int i = 0; i < len; i++) {
				if(i>0){
					str += "&";
				}
				str += list.get(temp[i]);
			}
			result.add(baseUrl+"?"+str);
		}
		return result;
	}

	@Override
	public List<String> getUserAccountsByDepCode(String orgCode) {
		return userMapper.getUserAccountsByDepCode(orgCode);
	}

	@Override
	public boolean dictTableWhiteListCheckBySql(String selectSql) {
		return dictTableWhiteListHandler.isPassBySql(selectSql);
	}

	@Override
	public boolean dictTableWhiteListCheckByDict(String tableOrDictCode, String... fields) {
		if (fields == null || fields.length == 0) {
			return dictTableWhiteListHandler.isPassByDict(tableOrDictCode);
		} else {
			return dictTableWhiteListHandler.isPassByDict(tableOrDictCode, fields);
		}
	}

	/**
	 * Tự động phát hành quy trình
	 * @param dataId
	 * @param currentUserName
	 */
	@Override
	public void announcementAutoRelease(String dataId, String currentUserName) {
		//Truy vấn thông báo theo ID
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(dataId);
		//Sau khi quy trình được phê duyệt sẽ tự động phát hành thông báo
		sysAnnouncement.setSendStatus(CommonSendStatus.PUBLISHED_STATUS_1);
		sysAnnouncement.setSendTime(new Date());
		sysAnnouncement.setSender(currentUserName);
		boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
		//Đẩy tin nhắn thông báo
		if(ok) {
			if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
				// Bổ sung quan hệ giữa thông báo và người dùng
				sysAnnouncementService.batchInsertSysAnnouncementSend(sysAnnouncement.getId(), sysAnnouncement.getTenantId());

				// Đẩy thông báo websocket
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
				obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
				obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
				webSocket.sendMessage(obj.toJSONString());
			}else {
				// 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
				String userId = sysAnnouncement.getUserIds();
				String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
				obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
				webSocket.sendMessage(userIds, obj.toJSONString());
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

	@Override
	public SysDepartModel queryCompByOrgCode(String orgCode) {
		AssertUtils.assertNotEmpty("Vui lòng nhập mã phòng ban",orgCode);
		SysDepart comp = sysDepartService.queryCompByOrgCode(orgCode);
		if(comp == null) {
			log.error("Không truy vấn được thông tin công ty tương ứng");
			return null;
		}
		SysDepartModel respData = new SysDepartModel();
		BeanUtils.copyProperties(comp, respData);
		return respData;
	}

    /**
     * Truy vấn công ty cấp trên theo mã phòng ban và cấp bậc
     * 
     * @param orgCode Mã phòng ban
     * @param level Có thể truyền rỗng, mặc định là 1, giá trị tối thiểu là 1
     * @return
     */
    @Override
    public SysDepartModel queryCompByOrgCodeAndLevel(String orgCode, Integer level) {
        if (null == level || 0 == level) {
            level = 1;
        }
        int codeNum = YouBianCodeUtil.ZHANWEI_LENGTH;

        //Kiểm tra cấp cha trước code
        String parendCode = "";
        if (orgCode.length() > codeNum) {
            parendCode = orgCode.substring(0, codeNum);
        } else {
            return null;
        }
        //Theo mã phòng ban, truy vấn dữ liệu của công ty và công ty con
        List<String> categoryList = new ArrayList<>();
        categoryList.add(DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue());
        categoryList.add(DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue());
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.like(SysDepart::getOrgCode, parendCode);
        query.in(SysDepart::getOrgCategory, categoryList);
        query.orderByAsc(SysDepart::getOrgType);
        List<SysDepart> sysDepartList = sysDepartService.list(query);
        if (!CollectionUtils.isEmpty(sysDepartList)) {
            //Lấy công ty cấp trên
            SysDepart depart = getParentCompanyByOrgCode(orgCode, sysDepartList, level, 1);
            if(depart == null){
                depart = sysDepartList.get(0);
            }
            SysDepartModel respData = new SysDepartModel();
            BeanUtils.copyProperties(depart, respData);
            return respData;
        }
        return null;
    }

    @Override
    public Object runAiragFlow(AiragFlowDTO airagFlowDTO) {
        if(oConvertUtils.isEmpty(airagFlowDTO.getFlowId())) {
            throw new JeecgBootException("ID quy trình không được để trống");
        }
        Result<Object> o = (Result<Object>)airagFlowService.runFlow(airagFlowDTO);
        return o.getResult();
    }

	@Override
	public SseEmitter runAiragFlowStream(AiragFlowDTO airagFlowDTO) {
		if (oConvertUtils.isEmpty(airagFlowDTO.getFlowId())) {
			throw new JeecgBootException("ID quy trình không được để trống");
		}
		return airagFlowService.runFlowStream(airagFlowDTO);
	}

	/**
	 * uniPush Đẩy tin nhắn cho người dùng APP
	 * @param pushMessageDTO
	 */
	@Override
	public void uniPushMsgToUser(PushMessageDTO pushMessageDTO) {
		//jeecg.unicloud.pushUrl Khi chưa cấu hình thì coi như chức năng đẩy chưa được bật, bỏ qua trực tiếp (tránh các kịch bản như tin nhắn định kỳ liên tục ghi log cảnh báo)
		if(oConvertUtils.isEmpty(jeecgPushUrl) || "''".equals(jeecgPushUrl) || "??".equals(jeecgPushUrl) ){
			log.warn("yml Mục cấu hình: jeecg.unicloud.pushUrl Chưa được thiết lập, tính năng đẩy tin nhắn UniPush của APP chưa được bật!");
			return;
		}
		log.info("UniappPush đẩy URL:{}", jeecgPushUrl);
		try {
			// Lấy thông tin người dùng được đẩy
			List<String> usernames = pushMessageDTO.getUsernames();
			List<String> userIds = pushMessageDTO.getUserIds();

			// Xây dựng clientIds
			List<String> clientIds = getClientIds(usernames, userIds);

			// Xây dựng tham số yêu cầu
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("title", pushMessageDTO.getTitle());
			requestBody.put("content", pushMessageDTO.getContent());
			requestBody.put("data", pushMessageDTO.getPayload());
			requestBody.put("request_id", String.valueOf(System.currentTimeMillis()));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Đẩy cho toàn bộ người dùng không cần clientIds, đẩy cho người dùng chỉ định cần thiết lập clientIds
			boolean isAllUserPush = CommonConstant.MSG_TYPE_ALL.equals(pushMessageDTO.getPushType());
			if (!isAllUserPush) {
				if (CollectionUtils.isEmpty(clientIds)) {
					log.warn("UniPush clientIds đẩy tin nhắn trống");
					return;
				}
				requestBody.put("cids", clientIds);
			}

			// Logic đẩy thống nhất
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
			ResponseEntity<Map> response = restTemplate.postForEntity(jeecgPushUrl, request, Map.class);

			// Xử lý phản hồi thống nhất
			String pushType = isAllUserPush ? "Tất cả người dùng" : "Người dùng đơn lẻ";
			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("{} UniPush Đẩy tin nhắn thành công, trả về response:{}", pushType, response.getBody());
			} else {
				log.error("{} UniPush Đẩy tin nhắn thất bại, trả về response:{}", pushType, response.getBody());
			}
		} catch (RestClientException e) {
			log.warn("UniAPP Đẩy tin nhắn bất thường:"+ e.getMessage());
		}
	}
	/**
	 * Lấy clientIds theo tên người dùng hoặc ID người dùng clientIds
	 */
	private List<String> getClientIds(List<String> usernames, List<String> userIds) {
		if (!CollectionUtils.isEmpty(usernames)) {
			return extractClientIds(this.queryUsersByUsernames(String.join(",", usernames)));
		} else if (!CollectionUtils.isEmpty(userIds)) {
			return extractClientIds(this.queryUsersByIds(String.join(",", userIds)));
		}
		return Collections.emptyList();
	}

	/**
	 * Trích xuất clientIds từ thông tin người dùng clientIds
	 */
	private List<String> extractClientIds(List<JSONObject> users) {
		return users.stream()
				.map(user -> user.getString("clientId"))
				.filter(clientId -> oConvertUtils.isNotEmpty(clientId) && !clientId.trim().isEmpty())
				.collect(Collectors.toList());
	}

	/**
     * Tìm cấp trên theo orgCode
     * 
     * @param orgCode
     * @param sysDepartList
     * @param level Chỉ định cấp thứ mấy, tính từ dưới lên trên
     * @param nowLevel
     * @return
     */
    public SysDepart getParentCompanyByOrgCode(String orgCode,List<SysDepart> sysDepartList, int level, int nowLevel) {
        //Lấy mã công ty cấp trên
        String code = this.getPrefix(orgCode);
        if(oConvertUtils.isEmpty(code)) {
            return null;
        }
        List<SysDepart> list = sysDepartList.stream().filter(sysDepart -> sysDepart.getOrgCode().equals(code)).toList();
        //Xác định cấp độ của cấp trên
        if(!CollectionUtils.isEmpty(list) && nowLevel == level) {
            return list.get(0);
        } else {
            if(!CollectionUtils.isEmpty(list)) {
                nowLevel++;
            }
            return getParentCompanyByOrgCode(code, sysDepartList, level, nowLevel);
        }
    }

    /**
     * Lấy tiền tố mã theo giá trị chỉ định (mỗi cấp cố định YouBianCodeUtil.ZHANWEI_LENGTH ký tự）
     *
     * @param fullCode Mã đầy đủ (ví dụ như"A01A01A01"）
     * @return Mã tiền tố sau khi trích xuất (ví dụ"A01A01"）
     */
    private String getPrefix(String fullCode) {
        if(fullCode.length() < YouBianCodeUtil.ZHANWEI_LENGTH){
            return "";
        }
        // Tính tổng số cấp,Theo ZHANWEI_LENGTH
        int totalLevels = fullCode.length() / YouBianCodeUtil.ZHANWEI_LENGTH;
        int keepLevels = totalLevels - 1;
        // Tính độ dài cần cắt (giữ lại số cấp × YouBianCodeUtil.ZHANWEI_LENGTH）
        int prefixLength = keepLevels * YouBianCodeUtil.ZHANWEI_LENGTH;
        return prefixLength == 0 ? "" : fullCode.substring(0, prefixLength);
    }

    /**
     * Theo code phòng ban hoặc id phòng ban, lấy tên phòng ban(phòng ban hiện tại và phòng ban cấp trên)
     *
     * @param orgCode Mã phòng ban
     * @param depId Phòng ban id
     * @return String Tên phòng ban
     */
    @Override
    public String getDepartPathNameByOrgCode(String orgCode, String depId) {
        return sysDepartService.getDepartPathNameByOrgCode(orgCode, depId);
    }
}
