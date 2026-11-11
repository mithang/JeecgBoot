package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.FileTypeEnum;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.SysFilesModel;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.entity.SysFormFile;
import org.jeecg.modules.system.mapper.SysCommentMapper;
import org.jeecg.modules.system.mapper.SysFormFileMapper;
import org.jeecg.modules.system.service.ISysCommentService;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.jeecg.modules.system.vo.UserAvatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: Bảng trả lời bình luận hệ thống
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Service
public class SysCommentServiceImpl extends ServiceImpl<SysCommentMapper, SysComment> implements ISysCommentService {

    @Autowired
    private ISysBaseAPI sysBaseApi;

    @Autowired
    private SysFormFileMapper sysFormFileMapper;

//    @Autowired
//    private IEasyOaBaseApi easyOaBseApi;

    @Autowired
    private RedisUtil redisUtil;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Value(value = "${jeecg.uploadType}")
    private String uploadType;

    /**
     * sysFormFile Tên bảng trong
     */
    private static final String SYS_FORM_FILE_TABLE_NAME = "sys_comment";

    @Override
    public List<SysCommentVO> queryFormCommentInfo(SysComment sysComment) {
        String tableName = sysComment.getTableName();
        String dataId = sysComment.getTableDataId();
        //Lấy thông tin bình luận
        List<SysCommentVO> list = this.baseMapper.queryCommentList(tableName, dataId);
        // Lấy nhân viên liên quan đến bình luận
        Set<String> personSet = new HashSet<>();
        if(list!=null && list.size()>0){
            for(SysCommentVO vo: list){
                if(oConvertUtils.isNotEmpty(vo.getFromUserId())){
                    personSet.add(vo.getFromUserId());    
                }
                if(oConvertUtils.isNotEmpty(vo.getToUserId())){
                    personSet.add(vo.getToUserId());
                }
            }
        }
        if(personSet.size()>0){
            //Lấy thông tin người dùng
            Map<String, UserAvatar> userAvatarMap = queryUserAvatar(personSet);
            for(SysCommentVO vo: list){
                String formId = vo.getFromUserId();
                String toId = vo.getToUserId();
                // Đặt ảnh đại diện, tên người dùng
                if(oConvertUtils.isNotEmpty(formId)){
                    UserAvatar fromUser = userAvatarMap.get(formId);
                    if(fromUser!=null){
                        vo.setFromUserId_dictText(fromUser.getRealname());
                        vo.setFromUserAvatar(fromUser.getAvatar());
                    }
                }
                if(oConvertUtils.isNotEmpty(toId)){
                    UserAvatar toUser = userAvatarMap.get(toId);
                    if(toUser!=null){
                        vo.setToUserId_dictText(toUser.getRealname());
                        vo.setToUserAvatar(toUser.getAvatar());
                    }
                }
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOneFileComment(HttpServletRequest request) {
        String existFileId = request.getParameter("fileId");
        if(oConvertUtils.isEmpty(existFileId)){
            String savePath = "";
            // Lấy đường dẫn nghiệp vụ
            String bizPath = request.getParameter("biz");
            // Lấy đối tượng tệp tải lên
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");

            // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
            try {
                SsrfFileTypeFilter.checkUploadFileType(file, bizPath);
            } catch (Exception e) {
                throw new JeecgBootException(e);
            }

            if (oConvertUtils.isEmpty(bizPath)) {
                bizPath = CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType) ? "upload" : "";
            }
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
                savePath = this.uploadLocal(file, bizPath);
            } else {
                savePath = CommonUtils.upload(file, bizPath, uploadType);
            }

            String orgName = file.getOriginalFilename();
            // Lấy tên tệp
            orgName = CommonUtils.getFileName(orgName);
            //Kích thước tệp
            long size = file.getSize();
            //Loại tệp
            String type = orgName.substring(orgName.lastIndexOf("."), orgName.length());
            FileTypeEnum fileType = FileTypeEnum.getByType(type);

            //Lưu vào SysFiles
            SysFilesModel sysFiles = new SysFilesModel();
            sysFiles.setFileName(orgName);
            sysFiles.setUrl(savePath);
            sysFiles.setFileType(fileType.getValue());
            sysFiles.setStoreType("temp");
            if (size > 0) {
                sysFiles.setFileSize(Double.parseDouble(String.valueOf(size)));
            }
            String fileId = String.valueOf(IdWorker.getId());
            sysFiles.setId(fileId);
            String tenantId = oConvertUtils.getString(TenantContext.getTenant());
            sysFiles.setTenantId(tenantId);
//            //update-begin---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Di chuyển backend kho tri thức---
//            easyOaBseApi.addSysFiles(sysFiles);
//            //update-end---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Di chuyển backend kho tri thức---

            //Lưu vào SysFormFile
            String tableName = SYS_FORM_FILE_TABLE_NAME;
            String tableDataId = request.getParameter("commentId");
            SysFormFile sysFormFile = new SysFormFile();
            sysFormFile.setTableName(tableName);
            sysFormFile.setFileType(fileType.getValue());
            sysFormFile.setTableDataId(tableDataId);
            sysFormFile.setFileId(fileId);
            sysFormFileMapper.insert(sysFormFile);

        }else{
//            //update-begin---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Di chuyển backend kho tri thức---
//            SysFilesModel sysFiles = easyOaBseApi.getFileById(existFileId);
//            //update-end---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Di chuyển backend kho tri thức---
//            if(sysFiles!=null){
                //Lưu vào SysFormFile
                String tableName = SYS_FORM_FILE_TABLE_NAME;
                String tableDataId = request.getParameter("commentId");
                SysFormFile sysFormFile = new SysFormFile();
                sysFormFile.setTableName(tableName);
                sysFormFile.setFileType("");
                sysFormFile.setTableDataId(tableDataId);
                sysFormFile.setFileId(existFileId);
                sysFormFileMapper.insert(sysFormFile);
//            }
        }
    }

    /**
     * app App trả lời bình luận, lưu file
     * @param request
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void appSaveOneFileComment(HttpServletRequest request) {

        String orgName = request.getParameter("fileName");
        String fileSize = request.getParameter("fileSize");
        String savePath = request.getParameter("savePath");
        // Lấy tên tệp
        orgName = CommonUtils.getFileName(orgName);
        //Kích thước tệp
        long size = Long.valueOf(fileSize);
        //Loại tệp
        String type = orgName.substring(orgName.lastIndexOf("."), orgName.length());
        FileTypeEnum fileType = FileTypeEnum.getByType(type);

        //Lưu vào SysFiles
        SysFilesModel sysFiles = new SysFilesModel();
        sysFiles.setFileName(orgName);
        sysFiles.setUrl(savePath);
        sysFiles.setFileType(fileType.getValue());
        sysFiles.setStoreType("temp");
        if (size > 0) {
            sysFiles.setFileSize(Double.parseDouble(String.valueOf(size)));
        }
        String defaultValue = "0";
        String fileId = String.valueOf(IdWorker.getId());
        sysFiles.setId(fileId);
        String tenantId = oConvertUtils.getString(TenantContext.getTenant());
        sysFiles.setTenantId(tenantId);
//        //update-begin---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Di chuyển backend kho tri thức---
//        easyOaBseApi.addSysFiles(sysFiles);
//        //update-end---author:wangshuai---date:2024-01-04---for:【QQYUN-7821】Di chuyển backend kho tri thức---
        //Lưu vào SysFormFile
        String tableName = SYS_FORM_FILE_TABLE_NAME;
        String tableDataId = request.getParameter("commentId");
        SysFormFile sysFormFile = new SysFormFile();
        sysFormFile.setTableName(tableName);
        sysFormFile.setFileType(fileType.getValue());
        sysFormFile.setTableDataId(tableDataId);
        sysFormFile.setFileId(fileId);
        sysFormFileMapper.insert(sysFormFile);

    }

    @Override
    public List<SysCommentFileVo> queryFormFileList(String tableName, String formDataId) {
        List<SysCommentFileVo> list = baseMapper.queryFormFileList(tableName, formDataId);
        return list;
    }

    @Override
    public String saveOne(SysComment sysComment) {
        this.save(sysComment);
        //Gửi tin nhắn hệ thống
        String content = sysComment.getCommentContent();
        if (content.indexOf("@") >= 0) {
            Set<String> set = getCommentUsername(content);
            if (set.size() > 0) {
                String users = String.join(",", set);
                MessageDTO md = new MessageDTO();
                md.setTitle("Có người đã nhắc đến bạn trong bình luận của biểu mẫu");
                md.setContent(content);
                md.setToAll(false);
                md.setToUser(users);
                md.setFromUser("system");
                md.setType(MessageTypeEnum.XT.getType());

                // Giải thích logic mã: QQYUN-4744【Thông báo hệ thống】6, thông báo hệ thống@Sau khi @ người khác, đối phương không thấy đó là biểu mẫu nào@, không có siêu liên kết
                String tableName = sysComment.getTableName();
                String prefix = "desform:";
                if (tableName != null) {
                    // Trình thiết kế biểu mẫu
                    if (tableName.startsWith(prefix)) {
                        Map<String, Object> data = new HashMap<>();
                        data.put(CommonConstant.NOTICE_MSG_BUS_TYPE, "comment");
                        JSONObject params = new JSONObject();
                        params.put("code", tableName.substring(prefix.length()));
                        params.put("dataId", sysComment.getTableDataId());
                        params.put("type", "designForm");
                        data.put(CommonConstant.NOTICE_MSG_SUMMARY, params);
                        md.setData(data);
                    }
                    // Online Form, kiểm tra có mang theo hay không id
                    else if (oConvertUtils.isNotEmpty(sysComment.getTableId())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put(CommonConstant.NOTICE_MSG_BUS_TYPE, "comment");
                        JSONObject params = new JSONObject();
                        params.put("code", tableName);
                        params.put("formId", sysComment.getTableId());
                        params.put("dataId", sysComment.getTableDataId());
                        params.put("type", "cgform");
                        data.put(CommonConstant.NOTICE_MSG_SUMMARY, params);
                        md.setData(data);
                    }
                }
                
                sysBaseApi.sendTemplateMessage(md);
            }
        }
        return sysComment.getId();
    }

    @Override
    public void deleteOne(String id) {
        this.removeById(id);
        //Còn phải xóa tệp liên kết
        LambdaQueryWrapper<SysFormFile> query = new LambdaQueryWrapper<SysFormFile>()
                .eq(SysFormFile::getTableDataId, id)
                .eq(SysFormFile::getTableName, SYS_FORM_FILE_TABLE_NAME);
        this.sysFormFileMapper.delete(query);
    }

    /**
     * Lấy tài khoản người dùng trong bình luận bằng biểu thức chính quy
     *
     * @return
     */
    private Set<String> getCommentUsername(String content) {
        Set<String> set = new HashSet<String>(3);
        String reg = "(@(.*?)\\[(.*?)\\])";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while (m.find()) {
            if (m.groupCount() == 3) {
                String username = m.group(3);
                set.add(username);
            }
        }
        return set;
    }


    /**
     * Tải file lên cục bộ
     *
     * @param mf      Tệp
     * @param bizPath Đường dẫn tùy chỉnh
     * @return
     */
    private String uploadLocal(MultipartFile mf, String bizPath) {
        try {
            // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
            SsrfFileTypeFilter.checkUploadFileType(mf, bizPath);
        } catch (Exception e) {
            throw new JeecgBootException(e);
        }
        
        try {
            String ctxPath = uploadpath;
            String fileName = null;
            //update-begin---author:liusq ---date:2026-03-30  for：【issues/9427] Sửa lỗ hổng path traversal của bizPath trong uploadLocal(CWE-22)-----------
            // Kiểm tra path traversal: sau khi chuẩn hóa, đảm bảo thư mục đích nằm trong uploadpath
            File uploadDir = new File(ctxPath).getCanonicalFile();
            File file = new File(ctxPath + File.separator + bizPath + File.separator).getCanonicalFile();
            if (!file.toPath().startsWith(uploadDir.toPath())) {
                throw new JeecgBootException("Đường dẫn nghiệp vụ không hợp lệ, cấm truy cập đường dẫn ngoài thư mục tải lên: " + bizPath);
            }
            //update-end---author:liusq ---date:2026-03-30  for：【issues/9427] Sửa lỗ hổng path traversal của bizPath trong uploadLocal(CWE-22)-----------
            if (!file.exists()) {
                file.mkdirs();// Tạo thư mục gốc của tệp
            }
            String orgName = mf.getOriginalFilename();// Lấy tên tệp
            orgName = CommonUtils.getFileName(orgName);
            if (orgName.indexOf(".") != -1) {
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
            } else {
                fileName = orgName + "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if (oConvertUtils.isNotEmpty(bizPath)) {
                dbpath = bizPath + File.separator + fileName;
            } else {
                dbpath = fileName;
            }
            if (dbpath.contains("\\")) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Truy vấn thông tin người dùng
     * @param idSet
     * @return
     */
    private Map<String, UserAvatar> queryUserAvatar(Set<String> idSet){
        List<UserAvatar> list = this.baseMapper.queryUserAvatarList(idSet);
        Map<String, UserAvatar> map = new HashMap<>();
        if(list!=null && list.size()>0){
            for(UserAvatar user: list){
                map.put(user.getId(), user);
            }
        }
        return map;
    }
    
}
