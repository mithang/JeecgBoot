package org.jeecg.modules.airag.llm.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @Description: Lớp công cụ thực thi dòng lệnh
 * @Author: chenrui
 * @Date: 2024/4/8 10:11
 */
@Slf4j
public class CommandExecUtil {


    /**
     * Thực thi dòng lệnh
     *
     * @param command
     * @param args
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2024/4/9 10:59
     */
    public static String execCommand(String command, String[] args) throws IOException {
        if (null == command || command.isEmpty()) {
            throw new IllegalArgumentException("Lệnh không được trống");
        }
        return execCommand(command.split(" "), args);
    }

    /**
     * Ký tự meta Shell bị cấm xuất hiện trong tham số (áp dụng cho Windows cmd và Unix shell)
     */
    private static final Pattern SHELL_INJECTION_PATTERN =
            Pattern.compile("[&|;<>`$!\\\\\\r\\n]");

    /**
     * Ký tự nguy hiểm bị cấm xuất hiện trong tên tệp (ngăn chèn lệnh qua tên tệp)
     */
    private static final Pattern FILENAME_INJECTION_PATTERN =
            Pattern.compile("[&|;<>`$!\"'\\r\\n]");

    /**
     * Kiểm tra từng tham số lệnh, từ chối tham số chứa ký tự chèn Shell
     *
     * @param arg Tham số cần kiểm tra
     * @throws IllegalArgumentException Nếu tham số chứa ký tự nguy hiểm
     */
    public static void validateArg(String arg) {
        if (arg != null && SHELL_INJECTION_PATTERN.matcher(arg).find()) {
            throw new IllegalArgumentException("Tham số lệnh chứa ký tự không hợp lệ, đã từ chối thực thi: " + arg);
        }
    }

    /**
     * Kiểm tra đường dẫn tệp, từ chối đường dẫn chứa ký tự nguy hiểm (ngăn chèn qua tên tệp)
     *
     * @param filePath Đường dẫn tệp cần kiểm tra
     * @throws IllegalArgumentException Nếu đường dẫn tệp chứa ký tự nguy hiểm
     */
    public static void validateFilePath(String filePath) {
        if (filePath != null && FILENAME_INJECTION_PATTERN.matcher(filePath).find()) {
            throw new IllegalArgumentException("Đường dẫn tệp chứa ký tự không hợp lệ, đã từ chối xử lý: " + filePath);
        }
    }

    /**
     * Thực thi dòng lệnh
     *
     * @param command thư mục script
     * @param args    Tham số
     * @author chenrui
     * @date 2024/4/09 10:30
     */
    public static String execCommand(String[] command, String[] args) throws IOException {

        if (null == command || command.length == 0) {
            throw new IllegalArgumentException("Lệnh không được trống");
        }

        if (null != args && args.length > 0) {
            // Kiểm tra từng tham số do người dùng kiểm soát, ngăn chèn lệnh
            for (String arg : args) {
                validateArg(arg);
            }
            command = (String[]) ArrayUtils.addAll(command, args);
        }

        // Dùng trực tiếp ProcessBuilder, không qua Shell hệ thống (ngăn chèn Shell)
        // Lưu ý: không dùng nữa cmd.exe /c hoặc /bin/sh -c, mảng tham số do JVM truyền trực tiếp cho hệ điều hành
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);

        Process process = null;
        try {
            log.debug(" =============================== Runtime command Script ===============================" );
            log.debug(String.join(" ", command));
            log.debug(" =============================== Runtime command Script =============================== " );
            process = Runtime.getRuntime().exec(command);
            try (ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
                 InputStream processInStream = new BufferedInputStream(process.getInputStream())) {
                new Thread(new InputStreamRunnable(process.getErrorStream(), "ErrorStream")).start();
                int num;
                byte[] bs = new byte[1024];
                while ((num = processInStream.read(bs)) != -1) {
                    resultOutStream.write(bs, 0, num);
                    String stepMsg = new String(bs);
//                    log.debug("Nhật ký dòng lệnh:" + stepMsg);
                    if (stepMsg.contains("input any key to continue...")) {
                        process.destroy();
                    }
                }
                String result = resultOutStream.toString();
                log.debug("Thực thi lệnh hoàn tất:" + result);
                return result;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * exec Lớp luồng lấy đầu ra console
     * Sử dụng luồng riêng để lấy đầu ra console,Ngăn luồng đầu vào bị chặn
     *
     * @author chenrui
     * @date 2024/4/09 10:30
     */
    static class InputStreamRunnable implements Runnable {
        BufferedReader bReader = null;
        String type = null;

        public InputStreamRunnable(InputStream is, String _type) {
            try {
                bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8));
                type = _type;
            } catch (Exception ex) {
            }
        }

        @SuppressWarnings("unused")
        public void run() {
            String line;
            int lineNum = 0;

            try {
                while ((line = bReader.readLine()) != null) {
                    lineNum++;
                    // Thread.sleep(200);
                }
                bReader.close();
            } catch (Exception ignored) {
            }
        }
    }
}
