package org.jeecg.test.security;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

/**
 * Phần mở rộng in kết quả kiểm thử chung: sau khi mỗi ca kiểm thử thực thi xong, tự động in một dòng kết quả,
 * Giải quyết"Khi kiểm thử đạt, console im lặng, trông như chưa chạy"gây bối rối.
 *
 * Cách dùng: thêm vào lớp kiểm thử @ExtendWith(PrintTestResultExtension.class)。
 * Đầu ra có dạng: [PASS] Khi xảy ra chuyển hướng, URL ban đầu và mục tiêu chuyển hướng đều được checkSsrfHttpUrl kiểm chứng một lần
 *
 * Lưu ý: dấu trạng thái dùng ASCII, tránh console Windows GBK bị lỗi ký tự; tên ca kiểm thử (@DisplayName tiếng Trung)
 * Trong IDE UTF-8 console có thể hiển thị bình thường.
 *
 * @author wangshuai
 * @date 2026-06-17
 */
public class PrintTestResultExtension implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("  [PASS] " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("  [FAIL] " + context.getDisplayName()
                + "  -> " + (cause == null ? "" : cause.getMessage()));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("  [ABORTED] " + context.getDisplayName());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("  [SKIP] " + context.getDisplayName()
                + reason.map(r -> "  (" + r + ")").orElse(""));
    }
}
