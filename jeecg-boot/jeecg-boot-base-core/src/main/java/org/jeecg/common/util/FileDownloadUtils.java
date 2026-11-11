package org.jeecg.common.util;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @program: file
 * @description: Tải xuống tệp
 * @author: chenrui
 * @date: 2019-05-24 16:34
 **/
@Slf4j
public class FileDownloadUtils {

    /**
     * Tải xuống một tệp
     *
     * @param response
     * @param storePath Địa chỉ lưu trữ tệp tải xuống
     * @param fileName  Tên tệp
     * @author: chenrui
     * @date: 2019/5/24 17:10
     */
    public static void downloadFile(HttpServletResponse response, String storePath, String fileName) {
        response.setCharacterEncoding("UTF-8");
        File file = new File(storePath);
        if (!file.exists()) {
            throw new NullPointerException("Specified file not found");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new NullPointerException("The file name can not null");
        }
        // Tải xuống tệp cấu hình
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        // Tệp tải xuống có thể hiển thị tiếng Trung bình thường
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        // Triển khai tải xuống tệp
        byte[] buffer = new byte[1024];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);) {
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Tải xuống nhiều tệp
     *
     * @param filesPath   Tập hợp tệp tải xuống
     * @param zipFileName Tên chung của nhiều tệp
     * @author: chenrui
     * @date: 2019/5/24 17:48
     */
    public static void downloadFileMulti(HttpServletResponse response, List<String> filesPath, String zipFileName) throws IOException {
        //Đặt tên gói nén
        String downloadName = zipFileName + ".zip";
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(downloadName, "UTF-8"));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        log.info("Bắt đầu nén tệp:" + filesPath);
        //Đặt luồng nén: ghi trực tiếp vào response, vừa nén vừa tải xuống
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
             DataOutputStream os = new DataOutputStream(zipOut);) {
            //Đặt phương pháp nén
            zipOut.setMethod(ZipOutputStream.DEFLATED);
            for (String filePath : filesPath) {
                //Lặp lại việc ghi tệp vào luồng nén
                File file = new File(filePath);
                if (file.exists()) {
                    //Thêm ZipEntry, và ghi luồng tệp vào ZipEntry, tức là nén tệp vào thư mục của tệp zip
                    String fileName = file.getName();
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    //Định dạng tệp luồng đầu ra

                    InputStream is = Files.newInputStream(file.toPath());
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = is.read(b)) != -1) {
                        os.write(b, 0, length);
                    }
                    is.close();
                    zipOut.closeEntry();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e);
        }
    }

    //update-begin---author:wangshuai ---date:2026-06-17  for：【issues/9681] Sửa lỗ hổng vượt qua chuyển hướng SSRF(CWE-918), cấm tự động chuyển hướng và kiểm chứng từng bước nhảy-----------
    /**
     * Mở một cách an toàn HTTP(S) kết nối: cấm tự động chuyển hướng, thực hiện kiểm chứng SSRF đối với URL ban đầu và từng đích chuyển hướng,
     * Trả về đối tượng kết nối cuối cùng đã kết nối (và đã vượt qua kiểm chứng).
     *
     * @param fileUrl Tệp URL
     * @return Đã kết nối HttpURLConnection
     * @throws IOException Ngoại lệ kết nối
     */
    private static HttpURLConnection openSafeConnection(String fileUrl) throws IOException {
        int maxRedirects = 5;
        String currentUrl = fileUrl;
        for (int i = 0; i <= maxRedirects; i++) {
            // Mỗi bước chuyển hướng (bao gồm URL ban đầu) đều thực hiện lại kiểm chứng SSRF, chặn loopback / link-local / Địa chỉ metadata đám mây
            SsrfFileTypeFilter.checkSsrfHttpUrl(currentUrl);

            HttpURLConnection conn = (HttpURLConnection) new URL(currentUrl).openConnection();
            // Điểm mấu chốt: cấm JDK tự động theo chuyển hướng, nếu không sẽ vượt qua kiểm chứng ở trên
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(30 * 1000);
            // Ngăn chặn chương trình chặn
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.connect();

            int code = conn.getResponseCode();
            if (code >= 300 && code < 400) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (oConvertUtils.isEmpty(location)) {
                    throw new JeecgBootException("Chuyển hướng không hợp lệ: Location trống");
                }
                // Xử lý chuyển hướng đường dẫn tương đối, phân tích thành địa chỉ tuyệt đối rồi quay lại đầu vòng lặp để kiểm chứng lại
                currentUrl = new URL(new URL(currentUrl), location).toString();
                continue;
            }
            return conn;
        }
        throw new JeecgBootException("URL không hợp lệ: số lần chuyển hướng quá nhiều");
    }
    //update-end---author:wangshuai ---date:2026-06-17  for：【issues/9681] Sửa lỗ hổng vượt qua chuyển hướng SSRF(CWE-918), cấm tự động chuyển hướng và kiểm chứng từng bước nhảy-----------

    //update-begin---author:liusq ---date:2026-06-29  for：【issues/9725】uploadImgByHttp tái sử dụng kết nối an toàn, sửa lỗ hổng bypass chuyển hướng SSRF(CWE-918)-----------
    /**
     * Tải tài nguyên từ mạng một cách an toàn thành mảng byte: bên trong tái sử dụng {@link #openSafeConnection(String)}，
     * Cấm tự động theo chuyển hướng, và thực hiện kiểm chứng SSRF đối với URL ban đầu và từng đích chuyển hướng.
     *
     * @param fileUrl Tệp URL
     * @return Mảng byte của tệp
     * @throws IOException Ngoại lệ kết nối hoặc đọc
     */
    public static byte[] download2BytesFromNet(String fileUrl) throws IOException {
        HttpURLConnection conn = openSafeConnection(fileUrl);
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP Yêu cầu thất bại, mã phản hồi: " + responseCode);
            }
            try (InputStream inputStream = conn.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } finally {
            conn.disconnect();
        }
    }
    //update-end---author:liusq ---date:2026-06-29  for：【issues/9725】uploadImgByHttp tái sử dụng kết nối an toàn, sửa lỗ hổng bypass chuyển hướng SSRF(CWE-918)-----------

    /**
     * Tải tài nguyên mạng xuống đĩa
     *
     * @param fileUrl
     * @param storePath
     * @author chenrui
     * @date 2024/1/19 10:09
     */
    public static String download2DiskFromNet(String fileUrl, String storePath) {
        //update-begin---author:liusq ---date:2026-03-30  for：【issues/9437】Sửa lỗ hổng path traversal của storePath trong download2DiskFromNet(CWE-22)-----------
        // Kiểm chứng duyệt đường dẫn: chặn ../ các ký tự duyệt đường dẫn tương tự, và đảm bảo đường dẫn đã chuẩn hóa khớp với đường dẫn gốc
        SsrfFileTypeFilter.checkPathTraversal(storePath);
        try {
            String canonicalPath = new File(storePath).getCanonicalPath();
            String absolutePath = new File(storePath).getAbsolutePath();
            if (!canonicalPath.equals(absolutePath)) {
                throw new JeecgBootException("Đường dẫn lưu trữ không hợp lệ, đường dẫn chứa ký tự duyệt đường dẫn: " + storePath);
            }
        } catch (IOException e) {
            throw new JeecgBootException("Kiểm chứng đường dẫn lưu trữ thất bại: " + storePath, e);
        }
        //update-end---author:liusq ---date:2026-03-30  for：【issues/9437】Sửa lỗ hổng path traversal của storePath trong download2DiskFromNet(CWE-22)-----------
        //update-begin---author:zhangdaihao ---date:2026-04-15  for：【issues/9553】Thêm kiểm chứng SSRF trước khi tải tài nguyên mạng-----------
        SsrfFileTypeFilter.checkSsrfHttpUrl(fileUrl);
        //update-end---author:zhangdaihao ---date:2026-04-15  for：【issues/9553】Thêm kiểm chứng SSRF trước khi tải tài nguyên mạng-----------
        try {
            //update-begin---author:wangshuai ---date:2026-06-17  for：【issues/9681】Sửa lỗ hổng bypass chuyển hướng SSRF, đổi sang kết nối an toàn cấm tự động chuyển hướng và kiểm chứng từng bước nhảy-----------
            // Mở kết nối an toàn: bên trong đã thực hiện kiểm chứng SSRF đối với URL ban đầu và từng mục tiêu chuyển hướng, đồng thời cấm tự động theo chuyển hướng
            HttpURLConnection conn = openSafeConnection(fileUrl);
            //update-end---author:wangshuai ---date:2026-06-17  for：【issues/9681】Sửa lỗ hổng bypass chuyển hướng SSRF, đổi sang kết nối an toàn cấm tự động chuyển hướng và kiểm chứng từng bước nhảy-----------
            // Đảm bảo thư mục tồn tại
            File file = ensureDestFileDir(storePath);
            try (InputStream inStream = conn.getInputStream();
                 FileOutputStream fs = new FileOutputStream(file);) {
                int byteread;
                byte[] buffer = new byte[1204];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                return storePath;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new JeecgBootException(e);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e);
        }
    }


    /**
     * Lấy tệp không trùng tên
     *
     * @param file
     * @return
     * @author chenrui
     * @date 2017 năm 24 tháng 5 buổi chiều 6:29:13
     * @version v0.0.1
     */
    public static File getUniqueFile(final File file) {
        if (!file.exists()) {
            return file;
        }

        File tmpFile = new File(file.getAbsolutePath());
        File parentDir = tmpFile.getParentFile();
        int count = 1;
        String extension = FilenameUtils.getExtension(tmpFile.getName());
        String baseName = FilenameUtils.getBaseName(tmpFile.getName());
        do {
            tmpFile = new File(parentDir, baseName + "(" + count++ + ")." + extension);
        } while (tmpFile.exists());
        return tmpFile;
    }

    /**
     * Đảm bảo thư mục tệp đầu ra
     *
     * @param destFilePath
     * @return
     * @author: chenrui
     * @date: 2019-05-21 16:49
     */
    private static File ensureDestFileDir(String destFilePath) {
        File destFile = new File(destFilePath);
        FileDownloadUtils.checkDirAndCreate(destFile.getParentFile());
        return destFile;
    }

    /**
     * Kiểm chứng thư mục tồn tại và tạo thư mục
     *
     * @param dir
     * @author chenrui
     * @date 2017 năm 24 tháng 5 buổi chiều 6:29:24
     * @version v0.0.1
     */
    public static void checkDirAndCreate(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    /**
     * Tải một tệp xuống luồng ZIP
     * Chức năng cốt lõi: lấy luồng tệp, ghi vào mục ZIP
     * @param fileUrl URL tệp (có thể là URL HTTP hoặc đường dẫn cục bộ)
     * @param fileName ZIP Tên tệp trong
     * @param zous ZIP Luồng đầu ra
     */
    public static void downLoadSingleFile(String fileUrl, String fileName, String uploadUrl,ZipArchiveOutputStream zous) {
        InputStream inputStream = null;
        try {
            // Tạo mục ZIP: mỗi tệp trong ZIP đều là một mục độc lập
            ZipArchiveEntry entry = new ZipArchiveEntry(fileName);
            zous.putArchiveEntry(entry);

            // Lấy luồng đầu vào của tệp: phân biệt tệp thông thường và lối tắt
            if (fileUrl.endsWith(".url")) {
                // Xử lý lối tắt: tạo.url Nội dung tệp
                inputStream = FileDownloadUtils.createInternetShortcut(fileName, fileUrl, "");
            } else {
                // Tải tệp thông thường: lấy luồng từ URL hoặc đường dẫn cục bộ
                inputStream = getDownInputStream(fileUrl,uploadUrl);
            }

            if (inputStream != null) {
                // Ghi luồng tệp vào ZIP
                IOUtils.copy(inputStream, zous);
            }
            // Đóng mục ZIP hiện tại
            zous.closeArchiveEntry();
        } catch (IOException e) {
            log.error("Tải xuống tệp thất bại: {}",  e);
        } finally {
            // Đảm bảo luồng đầu vào đã đóng
            IoUtil.close(inputStream);
        }
    }

    /**
     * Lấy luồng đầu vào của tệp tải xuống
     * Chức năng: lấy luồng tệp theo loại URL (HTTP hoặc cục bộ)
     * @param fileUrl URL tệp (hỗ trợ HTTP và đường dẫn cục bộ)
     * @return Luồng đầu vào của tệp, trả về khi thất bại null
     */
    public static InputStream getDownInputStream(String fileUrl, String uploadUrl) {
        try {
            // Xử lý URL HTTP: tải xuống qua mạng
            if (oConvertUtils.isNotEmpty(fileUrl) && fileUrl.startsWith(CommonConstant.STR_HTTP)) {
                //update-begin---author:zhangdaihao ---date:2026-04-15  for：【issues/9553】Sửa lỗ hổng SSRF lần hai, thực hiện kiểm chứng an toàn đối với URL tải xuống HTTP-----------
                SsrfFileTypeFilter.checkSsrfHttpUrl(fileUrl);
                //update-end---author:zhangdaihao ---date:2026-04-15  for：【issues/9553】Sửa lỗ hổng SSRF lần hai, thực hiện kiểm chứng an toàn đối với URL tải xuống HTTP-----------
                //update-begin---author:wangshuai ---date:2026-06-17  for：【issues/9681】Sửa lỗ hổng bypass chuyển hướng SSRF, đổi sang kết nối an toàn cấm tự động chuyển hướng và kiểm chứng từng bước nhảy-----------
                // Mở kết nối an toàn: bên trong đã thực hiện kiểm chứng SSRF đối với URL ban đầu và từng mục tiêu chuyển hướng, đồng thời cấm tự động theo chuyển hướng
                HttpURLConnection connection = openSafeConnection(fileUrl);
                //update-end---author:wangshuai ---date:2026-06-17  for：【issues/9681】Sửa lỗ hổng bypass chuyển hướng SSRF, đổi sang kết nối an toàn cấm tự động chuyển hướng và kiểm chứng từng bước nhảy-----------
                return connection.getInputStream();
            } else {
                // Xử lý tệp cục bộ: đọc trực tiếp hệ thống tệp
                String downloadFilePath = uploadUrl + File.separator + fileUrl;
                // Kiểm tra an toàn: ngăn tải xuống loại tệp nguy hiểm
                SsrfFileTypeFilter.checkDownloadFileType(downloadFilePath);
                return new BufferedInputStream(new FileInputStream(downloadFilePath));
            }
        } catch (IOException e) {
            // Khi có ngoại lệ trả về null, tầng trên sẽ xử lý trường hợp luồng rỗng
            return null;
        }
    }

    /**
     * Lấy phần mở rộng của tệp
     * Chức năng: trích xuất phần mở rộng từ tên tệp
     * @param fileName Tên tệp
     * @return Phần mở rộng tệp (không có dấu chấm), ví dụ như"txt"、"png"
     */
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * Tạo lối tắt (.url nội dung tệp)
     * Chức năng: tạo nội dung tệp lối tắt Internet
     * @param name Tên lối tắt
     * @param url Địa chỉ URL đích
     * @param icon Đường dẫn biểu tượng (tùy chọn)
     * @return Chứa.url Luồng đầu vào của nội dung tệp
     */
    public static InputStream createInternetShortcut(String name, String url, String icon) {
        StringWriter sw = new StringWriter();
        try {
            // Ghi nội dung theo định dạng lối tắt Windows
            sw.write("[InternetShortcut]\n");
            sw.write("URL=" + url + "\n");
            if (oConvertUtils.isNotEmpty(icon)) {
                sw.write("IconFile=" + icon + "\n");
            }
            // Chuyển đổi nội dung chuỗi thành luồng đầu vào
            return new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
        } finally {
            IoUtil.close(sw);
        }
    }
    /**
     * Trích xuất tên tệp từ URL
     * Chức năng: trích xuất tên tệp thuần từ URL HTTP hoặc đường dẫn cục bộ
     * @param fileUrl Tệp URL
     * @return Tên tệp (không có đường dẫn)
     */
    public static String getFileNameFromUrl(String fileUrl) {
        try {
            // Xử lý URL HTTP: trích xuất tên tệp từ phần đường dẫn
            if (fileUrl.startsWith(CommonConstant.STR_HTTP)) {
                URL url = new URL(fileUrl);
                String path = url.getPath();
                return path.substring(path.lastIndexOf('/') + 1);
            }

            // Xử lý đường dẫn tệp cục bộ: trích xuất tên tệp từ đường dẫn tệp
            return fileUrl.substring(fileUrl.lastIndexOf(File.separator) + 1);
        } catch (Exception e) {
            // Nếu phân tích thất bại, dùng timestamp làm tên tệp
            return "file_" + System.currentTimeMillis();
        }
    }
    /**
     * Tạo tên tệp trong ZIP
     * Chức năng: tránh xung đột tên tệp, thêm số thứ tự cho nhiều tệp
     * @param fileUrl URL tệp (dùng để trích xuất tên tệp gốc)
     * @param index Số thứ tự tệp (bắt đầu từ 0)
     * @param total Tổng số tệp
     * @return Tên tệp sau khi xử lý (có số thứ tự)
     */
    public static String generateFileName(String fileUrl, int index, int total) {
        // Trích xuất tên tệp gốc từ URL
        String originalFileName = getFileNameFromUrl(fileUrl);

        // Nếu chỉ có một tệp, dùng trực tiếp tên tệp gốc
        if (total == 1) {
            return originalFileName;
        }

        // Khi có nhiều tệp, dùng số thứ tự+Tên tệp gốc
        String extension = getFileExtension(originalFileName);
        String nameWithoutExtension = originalFileName.replace("." + extension, "");

        return String.format("%s_%d.%s", nameWithoutExtension, index + 1, extension);
    }
}
