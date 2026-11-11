package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.FileTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.util.HttpFileToMultipartFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * <p>
 * Bảng người dùng Controller frontend
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/common")
public class CommonController {

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * Local: local, MinIO: minio, Alibaba Cloud:alioss
     */
    @Value(value="${jeecg.uploadType}")
    private String uploadType;

    /**
     * @Author 政辉
     * @return
     */
    @GetMapping("/403")
    public Result<?> noauth()  {
        return Result.error("Không có quyền, vui lòng liên hệ quản trị viên để được cấp quyền!");
    }

    /**
     * Phương thức tải tệp lên thống nhất
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result<?> result = new Result<>();
        String savePath = "";
        String bizPath = request.getParameter("biz");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        
        // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
        SsrfFileTypeFilter.checkUploadFileType(file, bizPath);
  
        if (oConvertUtils.isEmpty(bizPath)) {
            bizPath = CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType) ? "upload" : "";
        }
        if(CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)){
            savePath = this.uploadLocal(file,bizPath);
        }else{
            savePath = CommonUtils.upload(file, bizPath, uploadType);
        }
        if(oConvertUtils.isNotEmpty(savePath)){
            
            //Thêm vào bảng file
            String orgName = file.getOriginalFilename();
            // Lấy tên tệp
            orgName = CommonUtils.getFileName(orgName);
            String type = orgName.substring(orgName.lastIndexOf(SymbolConstant.SPOT));
            FileTypeEnum fileType = FileTypeEnum.getByType(type);
            result.setMessage(savePath);
            result.setSuccess(true);
        }else {
            result.setMessage("Tải lên thất bại!");
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * Tải file lên cục bộ
     * @param mf Tệp
     * @param bizPath  Đường dẫn tùy chỉnh
     * @return
     */
    private String uploadLocal(MultipartFile mf,String bizPath){
        try {
            String ctxPath = uploadpath;
            String fileName = null;
            File file = new File(ctxPath + File.separator + bizPath + File.separator );
            if (!file.exists()) {
                // Tạo thư mục gốc của tệp
                file.mkdirs();
            }
            // Lấy tên tệp
            String orgName = mf.getOriginalFilename();
            orgName = CommonUtils.getFileName(orgName);
            if(orgName.indexOf(SymbolConstant.SPOT)!=-1){
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."));
            }else{
                fileName = orgName+ "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if(oConvertUtils.isNotEmpty(bizPath)){
                dbpath = bizPath + File.separator + fileName;
            }else{
                dbpath = fileName;
            }
            if (dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
                dbpath = dbpath.replace(SymbolConstant.DOUBLE_BACKSLASH, SymbolConstant.SINGLE_SLASH);
            }
            return dbpath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

//	@PostMapping(value = "/upload2")
//	public Result<?> upload2(HttpServletRequest request, HttpServletResponse response) {
//		Result<?> result = new Result<>();
//		try {
//			String ctxPath = uploadpath;
//			String fileName = null;
//			String bizPath = "files";
//			String tempBizPath = request.getParameter("biz");
//			if(oConvertUtils.isNotEmpty(tempBizPath)){
//				bizPath = tempBizPath;
//			}
//			String nowday = new SimpleDateFormat("yyyyMMdd").format(new Date());
//			File file = new File(ctxPath + File.separator + bizPath + File.separator + nowday);
//			if (!file.exists()) {
//				file.mkdirs();// Tạo thư mục gốc của tệp
//			}
//			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//			MultipartFile mf = multipartRequest.getFile("file");// Lấy đối tượng tệp tải lên
//			String orgName = mf.getOriginalFilename();// Lấy tên tệp
//			fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
//			String savePath = file.getPath() + File.separator + fileName;
//			File savefile = new File(savePath);
//			FileCopyUtils.copy(mf.getBytes(), savefile);
//			String dbpath = bizPath + File.separator + nowday + File.separator + fileName;
//			if (dbpath.contains("\\")) {
//				dbpath = dbpath.replace("\\", "/");
//			}
//			result.setMessage(dbpath);
//			result.setSuccess(true);
//		} catch (IOException e) {
//			result.setSuccess(false);
//			result.setMessage(e.getMessage());
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

    /**
     * Xem trước hình ảnh&Tải xuống tệp
     * Địa chỉ yêu cầu:http://localhost:8080/common/static/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
     *
     * @param request
     * @param response
     */
    @GetMapping(value = "/static/**")
    public void view(HttpServletRequest request, HttpServletResponse response) {
        // ISO-8859-1 ==> UTF-8 Thực hiện chuyển đổi mã hóa
        String imgPath = extractPathFromPattern(request);
        if(oConvertUtils.isEmpty(imgPath) || CommonConstant.STRING_NULL.equals(imgPath)){
            return;
        }
        
        try {
            imgPath = imgPath.replace("..", "").replace("../","");
            if (imgPath.endsWith(SymbolConstant.COMMA)) {
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            // Giải thích logic mã: Kiểm tra loại tệp tải xuống--------------
            SsrfFileTypeFilter.checkDownloadFileType(imgPath);

            String filePath = uploadpath + File.separator + imgPath;
            File file = new File(filePath);
            if(!file.exists()){
                response.setStatus(404);
                log.warn("Tệp["+imgPath+"]Không tồn tại..");
                return;
                //throw new RuntimeException();
            }
            // Thiết lập tải xuống bắt buộc, không mở
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
            
            // Kết hợp cách viết streaming của StreamingResponseBody
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("Xem trước tệp thất bại" + e.getMessage());
            response.setStatus(404);
            e.printStackTrace();
        }

    }

//	/**
//	 * Tải xuống tệp
//	 * Địa chỉ yêu cầu:http://localhost:8080/common/download/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
//	 *
//	 * @param request
//	 * @param response
//	 * @throws Exception
//	 */
//	@GetMapping(value = "/download/**")
//	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		// ISO-8859-1 ==> UTF-8 Thực hiện chuyển đổi mã hóa
//		String filePath = extractPathFromPattern(request);
//		// Các xử lý còn lại lược bỏ
//		InputStream inputStream = null;
//		OutputStream outputStream = null;
//		try {
//			filePath = filePath.replace("..", "");
//			if (filePath.endsWith(",")) {
//				filePath = filePath.substring(0, filePath.length() - 1);
//			}
//			String localPath = uploadpath;
//			String downloadFilePath = localPath + File.separator + filePath;
//			File file = new File(downloadFilePath);
//	         if (file.exists()) {
//	         	response.setContentType("application/force-download");// Thiết lập tải xuống bắt buộc, không mở            
//	 			response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
//	 			inputStream = new BufferedInputStream(new FileInputStream(file));
//	 			outputStream = response.getOutputStream();
//	 			byte[] buf = new byte[1024];
//	 			int len;
//	 			while ((len = inputStream.read(buf)) > 0) {
//	 				outputStream.write(buf, 0, len);
//	 			}
//	 			response.flushBuffer();
//	         }
//
//		} catch (Exception e) {
//			log.info("Tải xuống tệp thất bại" + e.getMessage());
//			// e.printStackTrace();
//		} finally {
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if (outputStream != null) {
//				try {
//					outputStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//	}

    /**
     * @Chức năng: xem trước PDF Iframe
     * @param modelAndView
     * @return
     */
    @RequestMapping("/pdf/pdfPreviewIframe")
    public ModelAndView pdfPreviewIframe(ModelAndView modelAndView) {
        modelAndView.setViewName("pdfPreviewIframe");
        return modelAndView;
    }

    /**
     *  Cắt toàn bộ chuỗi sau URL được chỉ định để làm tham số
     *  Làm như vậy là để tránh trường hợp URL chứa ký tự Trung Quốc hoặc ký tự đặc biệt (/v.v.) thì không khớp được
     * @param request
     * @return
     */
    private static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }

    /**
     * Tải lên máy chủ theo địa chỉ ảnh trên mạng
     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping("/uploadImgByHttp")
    public Result<String> uploadImgByHttp(@RequestBody JSONObject jsonObject, HttpServletRequest request){
        String fileUrl = oConvertUtils.getString(jsonObject.get("fileUrl"));
        String filename = oConvertUtils.getString(jsonObject.get("filename"));
        String bizPath = oConvertUtils.getString(jsonObject.get("bizPath"));
        try {
            String savePath = "";
            MultipartFile file = HttpFileToMultipartFileUtil.httpFileToMultipartFile(fileUrl, filename);
            // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
            SsrfFileTypeFilter.checkUploadFileType(file, bizPath);
            if (oConvertUtils.isEmpty(bizPath)) {
                bizPath = CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType) ? "upload" : "";
            }
            if(CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)){
                savePath = this.uploadLocal(file,bizPath);
            }else{
                savePath = CommonUtils.upload(file, bizPath, uploadType);
            }
            return Result.OK(savePath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

}
