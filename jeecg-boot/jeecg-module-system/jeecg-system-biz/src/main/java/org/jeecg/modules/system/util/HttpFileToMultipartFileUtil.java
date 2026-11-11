package org.jeecg.modules.system.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.MyCommonsMultipartFile;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Description: http Chuyển đổi tệp MultipartFile
 * @author: wangshuai
 * @date: 2025/11/5 17:55
 */
public class HttpFileToMultipartFileUtil {

    /**
     * Lấy
     *
     * @param fileUrl
     * @param filename
     * @return
     * @throws Exception
     */
    public static MultipartFile httpFileToMultipartFile(String fileUrl, String filename) throws Exception {
        //update-begin---author:liusq ---date:2026-06-29  for：【issues/9725] Sửa lỗ hổng vượt qua chuyển hướng SSRF(CWE-918), chuyển sang dùng tải xuống an toàn với cấm tự động chuyển hướng và kiểm tra SSRF từng bước nhảy-----------
        // Bản triển khai gốc chỉ kiểm tra SSRF đối với fileUrl một lần trước khi tải xuống, trong khi HttpURLConnection mặc định tự động theo chuyển hướng 3xx và không kiểm tra lại,
        // Kẻ tấn công có thể dùng 302 để chuyển hướng đến 127.0.0.1 / 169.254.169.254 và các địa chỉ nội bộ hoặc địa chỉ metadata đám mây khác để vượt qua kiểm tra.
        // Đổi sang dùng FileDownloadUtils.download2BytesFromNet, bên trong cấm tự động chuyển hướng và thực hiện kiểm tra SSRF đối với URL ban đầu và từng đích chuyển hướng.
        byte[] bytes = FileDownloadUtils.download2BytesFromNet(fileUrl);
        //update-end---author:liusq ---date:2026-06-29  for：【issues/9725] Sửa lỗ hổng vượt qua chuyển hướng SSRF(CWE-918), chuyển sang dùng tải xuống an toàn với cấm tự động chuyển hướng và kiểm tra SSRF từng bước nhảy-----------
        return convertByteToMultipartFile(bytes, filename);
    }

    /**
     * Tải dữ liệu hình ảnh
     */
    private static byte[] downloadImageData(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setRequestProperty("Accept", "image/*");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP Yêu cầu thất bại, mã phản hồi: " + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Chuyển byte thành MultipartFile
     *
     * @param data
     * @param fileName
     * @return
     */
    private static MultipartFile convertByteToMultipartFile(byte[] data, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory();
        FileItem item = factory.createItem(fileName, "application/octet-stream", true, fileName);

        try (OutputStream os = item.getOutputStream();
             ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Chuyển đổi mảng byte thất bại", e);
        }

        try {
            return new MyCommonsMultipartFile(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}