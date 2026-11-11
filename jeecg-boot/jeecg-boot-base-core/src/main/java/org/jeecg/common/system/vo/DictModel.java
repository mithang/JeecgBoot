package org.jeecg.common.system.vo;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Lớp từ điển
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictModel implements Serializable{
	private static final long serialVersionUID = 1L;

	public DictModel() {
	}
	
	public DictModel(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public DictModel(String value, String text, String color) {
		this.value = value;
		this.text = text;
		this.color = color;
	}

	/**
	 * Từ điển value
	 */
	private String value;
	/**
	 * Văn bản danh mục
	 */
	private String text;
	/**
	 * Màu từ điển
	 */
	private String color;

	/**
	 * Mục đích đặc biệt: JgEditableTable
	 * @return
	 */
	public String getTitle() {
		return this.text;
	}
	/**
	 * Mục đích đặc biệt: component Select của vue3
	 */
	public String getLabel() {
		return this.text;
	}


	/**
	 * Dùng cho trình thiết kế biểu mẫu để lưu trữ dữ liệu bảng bản ghi liên kết
	 * QQYUN-5595【Trình thiết kế biểu mẫu】Trường của bảng khác khi nhập chưa được dịch
	 */
	private JSONObject jsonObject;

}
