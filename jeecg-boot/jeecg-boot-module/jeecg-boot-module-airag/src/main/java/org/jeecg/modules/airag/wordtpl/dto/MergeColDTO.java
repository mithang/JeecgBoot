package org.jeecg.modules.airag.wordtpl.dto;

import lombok.Data;

/**
 * Gộp cột DTO
 * @author chenrui
 * @date 2025/7/4 18:36
 */
@Data
public class MergeColDTO {

	/**
	 * Số hàng của cột gộp
	 */
	private int row;

	/**
	 * Số cột bắt đầu của cột gộp
	 */
	private int from;

	/**
	 * Số cột kết thúc của cột gộp
	 */
	private int to;
}
