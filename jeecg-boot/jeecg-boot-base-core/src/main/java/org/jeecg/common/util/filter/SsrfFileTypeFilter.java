package org.jeecg.common.util.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Description: Kiểm chứng hậu tố nhạy cảm của tệp
 * @author: lsq
 * @date: 2023 năm 09 tháng 12 ngày 15:29
 */
@Slf4j
public class SsrfFileTypeFilter {

    /**
     * Danh sách trắng các loại tệp được phép thao tác
     */
    private final static List<String> FILE_TYPE_WHITE_LIST = new ArrayList<>();
    /**Khởi tạo loại header tệp, nếu chưa đủ thì tự bổ sung*/
    final static HashMap<String, String> FILE_TYPE_MAP = new HashMap<>();
    static {
        //Tệp hình ảnh
        FILE_TYPE_WHITE_LIST.add("jpg");
        FILE_TYPE_WHITE_LIST.add("jpeg");
        FILE_TYPE_WHITE_LIST.add("png");
        FILE_TYPE_WHITE_LIST.add("gif");
        FILE_TYPE_WHITE_LIST.add("bmp");
        FILE_TYPE_WHITE_LIST.add("svg");
        FILE_TYPE_WHITE_LIST.add("ico");
        FILE_TYPE_WHITE_LIST.add("heic");

        //Tệp văn bản
        FILE_TYPE_WHITE_LIST.add("txt");
        FILE_TYPE_WHITE_LIST.add("doc");
        FILE_TYPE_WHITE_LIST.add("docx");
        FILE_TYPE_WHITE_LIST.add("pdf");
        FILE_TYPE_WHITE_LIST.add("csv");
//        FILE_TYPE_WHITE_LIST.add("xml");
        FILE_TYPE_WHITE_LIST.add("md");

        //Tệp âm thanh/video
        FILE_TYPE_WHITE_LIST.add("mp4");
        FILE_TYPE_WHITE_LIST.add("avi");
        FILE_TYPE_WHITE_LIST.add("mov");
        FILE_TYPE_WHITE_LIST.add("wmv");
        FILE_TYPE_WHITE_LIST.add("mp3");
        FILE_TYPE_WHITE_LIST.add("wav");

        //Tệp bảng tính
        FILE_TYPE_WHITE_LIST.add("xls");
        FILE_TYPE_WHITE_LIST.add("xlsx");

        //Tệp nén
        FILE_TYPE_WHITE_LIST.add("zip");
        FILE_TYPE_WHITE_LIST.add("rar");
        FILE_TYPE_WHITE_LIST.add("7z");
        FILE_TYPE_WHITE_LIST.add("tar");

        //app Hậu tố tệp
        FILE_TYPE_WHITE_LIST.add("apk");
        FILE_TYPE_WHITE_LIST.add("wgt");

        //Hậu tố tệp trình chiếu
        FILE_TYPE_WHITE_LIST.add("ppt");
        FILE_TYPE_WHITE_LIST.add("pptx");

        //Đặt dấu header của tệp bị cấm
        FILE_TYPE_MAP.put("3c25402070616765206c", "jsp");
        FILE_TYPE_MAP.put("3c3f7068700a0a2f2a2a0a202a205048", "php");
        FILE_TYPE_MAP.put("cafebabe0000002e0041", "class");
        FILE_TYPE_MAP.put("494e5345525420494e54", "sql");
       /* fileTypeMap.put("ffd8ffe000104a464946", "jpg");
        fileTypeMap.put("89504e470d0a1a0a0000", "png");
        fileTypeMap.put("47494638396126026f01", "gif");
        fileTypeMap.put("49492a00227105008037", "tif");
        fileTypeMap.put("424d228c010000000000", "bmp");
        fileTypeMap.put("424d8240090000000000", "bmp");
        fileTypeMap.put("424d8e1b030000000000", "bmp");
        fileTypeMap.put("41433130313500000000", "dwg");
        fileTypeMap.put("3c21444f435459504520", "html");
        fileTypeMap.put("3c21646f637479706520", "htm");
        fileTypeMap.put("48544d4c207b0d0a0942", "css");
        fileTypeMap.put("696b2e71623d696b2e71", "js");
        fileTypeMap.put("7b5c727466315c616e73", "rtf");
        fileTypeMap.put("38425053000100000000", "psd");
        fileTypeMap.put("46726f6d3a203d3f6762", "eml");
        fileTypeMap.put("d0cf11e0a1b11ae10000", "doc");
        fileTypeMap.put("5374616E64617264204A", "mdb");
        fileTypeMap.put("252150532D41646F6265", "ps");
        fileTypeMap.put("255044462d312e350d0a", "pdf");
        fileTypeMap.put("2e524d46000000120001", "rmvb");
        fileTypeMap.put("464c5601050000000900", "flv");
        fileTypeMap.put("00000020667479706d70", "mp4");
        fileTypeMap.put("49443303000000002176", "mp3");
        fileTypeMap.put("000001ba210001000180", "mpg");
        fileTypeMap.put("3026b2758e66cf11a6d9", "wmv");
        fileTypeMap.put("52494646e27807005741", "wav");
        fileTypeMap.put("52494646d07d60074156", "avi");
        fileTypeMap.put("4d546864000000060001", "mid");
        fileTypeMap.put("504b0304140000000800", "zip");
        fileTypeMap.put("526172211a0700cf9073", "rar");
        fileTypeMap.put("235468697320636f6e66", "ini");
        fileTypeMap.put("504b03040a0000000000", "jar");
        fileTypeMap.put("4d5a9000030000000400", "exe");
        fileTypeMap.put("3c25402070616765206c", "jsp");
        fileTypeMap.put("4d616e69666573742d56", "mf");
        fileTypeMap.put("3c3f786d6c2076657273", "xml");
        fileTypeMap.put("494e5345525420494e54", "sql");
        fileTypeMap.put("7061636b616765207765", "java");
        fileTypeMap.put("406563686f206f66660d", "bat");
        fileTypeMap.put("1f8b0800000000000000", "gz");
        fileTypeMap.put("6c6f67346a2e726f6f74", "properties");
        fileTypeMap.put("cafebabe0000002e0041", "class");
        fileTypeMap.put("49545346030000006000", "chm");
        fileTypeMap.put("04000000010000001300", "mxp");
        fileTypeMap.put("504b0304140006000800", "docx");
        fileTypeMap.put("6431303a637265617465", "torrent");
        fileTypeMap.put("6D6F6F76", "mov");
        fileTypeMap.put("FF575043", "wpd");
        fileTypeMap.put("CFAD12FEC5FD746F", "dbx");
        fileTypeMap.put("2142444E", "pst");
        fileTypeMap.put("AC9EBD8F", "qdf");
        fileTypeMap.put("E3828596", "pwl");
        fileTypeMap.put("2E7261FD", "ram");*/
    }

    /**
     * @param fileName
     * @return String
     * @description Lấy loại tệp qua hậu tố tên tệp
     */
    private static String getFileTypeBySuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }


    /**
     * Lọc loại tệp tải xuống
     *
     * @param filePath
     */
    public static void checkDownloadFileType(String filePath) throws IOException {
        //Hậu tố tệp
        String suffix = getFileTypeBySuffix(filePath);
        log.debug(" 【Kiểm chứng tải xuống tệp】 Hậu tố tệp suffix: {}", suffix);
        boolean isAllowExtension = FILE_TYPE_WHITE_LIST.contains(suffix.toLowerCase());
        //Tệp có được phép tải xuống hay không
        if (!isAllowExtension) {
            throw new JeecgBootException("Tải xuống thất bại, tồn tại loại tệp không hợp lệ:" + suffix);
        }
    }

    /**
     * Lọc loại tệp tải lên
     *
     * @param file
     */
    public static void checkUploadFileType(MultipartFile file) throws Exception {
        checkUploadFileType(file, null);
    }
    
    /**
     * Lọc loại tệp tải lên
     *
     * @param file
     */
    public static void checkUploadFileType(MultipartFile file, String customPath) throws Exception {
        //1. Kiểm chứng an toàn đường dẫn
        validatePathSecurity(customPath);
        //2. Kiểm chứng hậu tố và header của tệp
        String suffix = getFileType(file, customPath);
        log.info("【Kiểm chứng tải lên tệp】 Hậu tố tệp suffix: {}，customPath：{}", suffix, customPath);
        boolean isAllowExtension = FILE_TYPE_WHITE_LIST.contains(suffix.toLowerCase());
        //Tệp có được phép tải xuống hay không
        if (!isAllowExtension) {
            throw new JeecgBootException("Tải lên thất bại, tồn tại loại tệp không hợp lệ:" + suffix);
        }
        //3. SVG Kiểm chứng an toàn nội dung tệp (issues/9693）
        if ("svg".equalsIgnoreCase(suffix)) {
            checkSvgSafety(file);
        }
    }

    /**
     * Lấy loại tệp bằng cách đọc header tệp
     *
     * @param file
     * @return Loại tệp
     * @throws Exception
     */

    private static String getFileType(MultipartFile file, String customPath) throws Exception {
        // Giải thích logic mã: [issue/4672]Phương thức này khiến tệp bị chiếm dụng; chú thích lại phương thức này thì Tomcat có thể tự động dọn các tệp tạm
        String fileExtendName = null;
        InputStream is = null;
        try {
            //is = new FileInputStream(file);
            is = file.getInputStream();
            byte[] b = new byte[10];
            is.read(b, 0, b.length);
            String fileTypeHex = String.valueOf(bytesToHexString(b));
            Iterator<String> keyIter = FILE_TYPE_MAP.keySet().iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                //update-begin---author:lsq ---date:2026-05-26  for：Sửa lỗi tệp SVG bị nhận nhầm thành php (<?xml và<?php trước 2.5 byte giống nhau, mở rộng độ dài so sánh lên 10 để tránh nhận nhầm)-----------
                // Kiểm chứng so sánh 10 ký tự đầu (5 byte), tránh <?xml và<?php bị phán đoán nhầm do 5 ký tự hex đầu giống nhau
                int compareLen = Math.min(10, Math.min(key.length(), fileTypeHex.length()));
                if (key.toLowerCase().startsWith(fileTypeHex.toLowerCase().substring(0, compareLen))
                        || fileTypeHex.toLowerCase().substring(0, compareLen).startsWith(key.toLowerCase())) {
                //update-end---author:lsq ---date:2026-05-26  for：Sửa lỗi tệp SVG bị nhận nhầm thành php (<?xml và<?php trước 2.5 byte giống nhau, mở rộng độ dài so sánh lên 10 để tránh nhận nhầm)-----------
                    fileExtendName = FILE_TYPE_MAP.get(key);
                    break;
                }
            }
            log.debug("-----Loại tệp đã chỉ định đã lấy được------"+fileExtendName);
            // Nếu không phải các loại trên thì phán đoán phần mở rộng
            if (StringUtils.isBlank(fileExtendName)) {
                String fileName = file.getOriginalFilename();
                // Nếu không có phần mở rộng thì trả về trực tiếp chuỗi rỗng
                if (-1 == fileName.indexOf(".")) {
                    return "";
                }
                // Nếu có phần mở rộng thì trả về phần mở rộng
                return getFileTypeBySuffix(fileName);
            }
            is.close();
            return fileExtendName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Lấy chuỗi header tệp
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Kiểm chứng an toàn đường dẫn
     */
    private static void validatePathSecurity(String customPath) throws JeecgBootException {
        if (customPath == null || customPath.trim().isEmpty()) {
            return;
        }

        // Thống nhất ký tự phân cách thành  /
        String normalized = customPath.replace("\\", "/");

        // 1. Ngăn chặn tấn công duyệt đường dẫn
        if (normalized.contains("..") || normalized.contains("~")) {
            throw new JeecgBootException("Đường dẫn nghiệp vụ tải lên chứa ký tự không hợp lệ!");
        }

        // 2. Giới hạn độ sâu đường dẫn
        int depth = normalized.split("/").length;
        if (depth > 5) {
            throw new JeecgBootException("Độ sâu đường dẫn nghiệp vụ tải lên vượt quá giới hạn!");
        }

        // 3. Giới hạn bộ ký tự (chỉ cho phép chữ cái, chữ số, dấu gạch dưới, dấu gạch ngang, dấu gạch chéo)
        if (!normalized.matches("^[a-zA-Z0-9/_-]+$")) {
            throw new JeecgBootException("Đường dẫn nghiệp vụ tải lên chứa ký tự không hợp lệ!");
        }
    }

    /**
     * Kiểm chứng tính an toàn của đường dẫn tệp, ngăn chặn tấn công duyệt đường dẫn
     * @param filePath Đường dẫn tệp
     */
    public static void checkPathTraversal(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return;
        }
        // 1. Ngăn chặn duyệt đường dẫn: không cho phép  ..
        if (filePath.contains("..")) {
            throw new JeecgBootException("Đường dẫn tệp chứa ký tự không hợp lệ");
        }
        // 2. Ngăn chặn vượt qua bằng mã hóa URL:%2e = .
        String fileLower = filePath.toLowerCase();
        if (fileLower.contains("%2e")) {
            throw new JeecgBootException("Đường dẫn tệp chứa ký tự không hợp lệ");
        }
    }

    /** 
     * 【issues/9553】Sửa lỗ hổng SSRF lần hai, thực hiện kiểm chứng an toàn đối với URL tải xuống HTTP
     * 2026-04-15
     * 
     * Kiểm chứng HTTP(S) URL, ngăn chặn tấn công SSRF, xem quy tắc tại  {@link LocalAddressCheckUtils#checkUrl(String)}
     *
     * @param fileUrl HTTP(S) URL
     */
    public static void checkSsrfHttpUrl(String fileUrl) {
        LocalAddressCheckUtils.checkUrl(fileUrl);
    }

    /**
     * Kiểm chứng hàng loạt tính an toàn của đường dẫn tệp (nhiều đường dẫn tệp phân cách bằng dấu phẩy)
     * @param files Đường dẫn tệp phân cách bằng dấu phẩy
     */
    public static void checkPathTraversalBatch(String files) {
        if (StringUtils.isBlank(files)) {
            return;
        }
        for (String file : files.split(",")) {
            if (StringUtils.isNotBlank(file)) {
                checkPathTraversal(file.trim());
            }
        }
    }

    /**
     * SVG Danh sách đen thẻ nguy hiểm (tên thẻ được so sánh thống nhất ở dạng chữ thường)
     */
    private static final Set<String> SVG_DANGEROUS_TAGS = new HashSet<>(Arrays.asList(
            "script", "foreignobject", "iframe", "object", "embed", "applet",
            "form", "input", "textarea", "button", "select",
            "link", "meta", "base", "import",
            "handler", "set", "animate", "animatemotion", "animatetransform"
    ));

    /**
     * SVG Regex thuộc tính nguy hiểm: khớp thuộc tính sự kiện (on*="...") và  javascript: Giao thức
     */
    private static final Pattern SVG_EVENT_ATTR_PATTERN = Pattern.compile(
            "\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SVG_JS_PROTOCOL_PATTERN = Pattern.compile(
            "javascript\\s*:", Pattern.CASE_INSENSITIVE
    );
    /**
     * HTML entity giao thức javascript đã mã hoá (&#106;avascript: và các biến thể khác)
     */
    private static final Pattern SVG_ENTITY_JS_PATTERN = Pattern.compile(
            "&#\\d+;|&#x[0-9a-f]+;", Pattern.CASE_INSENSITIVE
    );

    /**
     * Kiểm chứng nội dung tệp SVG có an toàn không, ngăn chặn XSS lưu trữ (issues/9693）。
     * Dùng phương thức quét văn bản để phát hiện thẻ nguy hiểm, thuộc tính sự kiện và  javascript: giao thức.
     *
     * @param file Tệp SVG đã tải lên
     */
    private static void checkSvgSafety(MultipartFile file) throws Exception {
        String originalContent;
        try (InputStream is = file.getInputStream()) {
            originalContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        String content = originalContent.toLowerCase();
        // Phát hiện thẻ nguy hiểm
        for (String tag : SVG_DANGEROUS_TAGS) {
            if (content.contains("<" + tag + ">") || content.contains("<" + tag + " ")
                    || content.contains("<" + tag + "/") || content.contains("<" + tag + "\t")
                    || content.contains("<" + tag + "\n") || content.contains("<" + tag + "\r")) {
                throw new JeecgBootException("Tải lên thất bại, tệp SVG chứa thẻ không an toàn:<" + tag + ">");
            }
        }
        // Phát hiện thuộc tính sự kiện (onclick, onload, onerror, onbegin, v.v.)
        if (SVG_EVENT_ATTR_PATTERN.matcher(originalContent).find()) {
            throw new JeecgBootException("Tải lên thất bại, tệp SVG chứa thuộc tính sự kiện không an toàn");
        }
        // Kiểm tra javascript: Giao thức
        if (SVG_JS_PROTOCOL_PATTERN.matcher(originalContent).find()) {
            throw new JeecgBootException("Tải lên thất bại, tệp SVG chứa giao thức javascript không an toàn");
        }
        // Phát hiện mã hoá HTML entity (ngăn chặn  &#106;avascript: và các cách vượt qua khác)
        if (SVG_ENTITY_JS_PATTERN.matcher(originalContent).find()) {
            throw new JeecgBootException("Tải lên thất bại, tệp SVG chứa nội dung đã mã hoá không an toàn");
        }
        // Kiểm tra DOCTYPE/ENTITY khai báo (ngăn chặn  XML Bomb / Billion Laughs DoS tấn công)
        if (content.contains("<!doctype") || content.contains("<!entity")) {
            throw new JeecgBootException("Tải lên thất bại, tệp SVG chứa nội dung không an toàn DOCTYPE/ENTITY Khai báo");
        }
    }

}
