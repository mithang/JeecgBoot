package org.jeecg.common.airag.api;

import org.jeecg.common.api.dto.AiragFlowDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * airag Chạy quy trình làm việc API
 * Để các module cơ sở như system phụ thuộc mềm vào khả năng AI workflow, phần triển khai do jeecg-boot-module-airag-flow mô-đun cung cấp;
 * Khi chưa tích hợp module AI thì không có bean triển khai, bên inject dùng ObjectProvider để kiểm tra null là có thể khởi động bình thường
 *
 * @author zhang
 * @date 2026-09-10
 */
public interface IAiragFlowApi {

	/**
	 * Chạy quy trình AI
	 *
	 * @param airagFlowDTO Tham số chạy quy trình
	 * @return Kết quả thực thi quy trình
	 */
	Object runFlow(AiragFlowDTO airagFlowDTO);

	/**
	 * Chạy luồng AI theo dạng streaming
	 *
	 * @param airagFlowDTO Tham số chạy quy trình
	 * @return Luồng SSE
	 */
	SseEmitter runFlowStream(AiragFlowDTO airagFlowDTO);
}
