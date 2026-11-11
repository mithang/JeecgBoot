package org.jeecg.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.dict.service.AutoPoiDictServiceI;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Mô tả: Annotation AutoPoi Excel hỗ trợ đặt tham số từ điển
 *  Ví dụ: @Excel(name = "Giới tính", width = 15, dicCode = "sex")
 * 1、Khi xuất sẽ căn cứ cấu hình danh mục để chuyển giá trị 1,2 Dịch thành：Nam, Nữ;
 * 2、Khi nhập sẽ dịch Nam, Nữ thành 1,2 Lưu vào cơ sở dữ liệu;
 * 
 * @Author:scott 
 * @since：2019-04-09 
 * @Version:1.0
 */
@Slf4j
@Lazy(false)
@Service
public class AutoPoiDictConfig implements AutoPoiDictServiceI {
	final static String EXCEL_SPLIT_TAG = "_";
	final static String TEMP_EXCEL_SPLIT_TAG = "---";

	@Lazy
	@Resource
	private CommonAPI commonApi;

	/**
	 * Tra cứu easypoi qua từ điển, văn bản từ điển cần thiết
	 * 
	 * @Author:scott 
	 * @since：2019-04-09
	 * @return
	 */
	@Override
	public String[] queryDict(String dicTable, String dicCode, String dicText) {
		List<String> dictReplaces = new ArrayList<String>();
		List<DictModel> dictList = null;
		// step.1 Nếu không có bảng từ điển thì dùng bảng từ điển hệ thống
		if (oConvertUtils.isEmpty(dicTable)) {
			dictList = commonApi.queryDictItemsByCode(dicCode);
		} else {
			try {
				dicText = oConvertUtils.getString(dicText, dicCode);
				dictList = commonApi.queryTableDictItemsByCode(dicTable, dicText, dicCode);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}


		for (DictModel t : dictList) {
			// Giải thích logic mã: [issues/4917]excel Ngoại lệ khi xuất---
			if(t!=null && t.getText()!=null && t.getValue()!=null){
				// Giải thích logic mã: [issues/I4MBB3]@Excel dicText Khi giá trị của trường có dấu gạch dưới, chức năng nhập không thể phân giải đúng---
				if(t.getValue().contains(EXCEL_SPLIT_TAG)){
					String val = t.getValue().replace(EXCEL_SPLIT_TAG,TEMP_EXCEL_SPLIT_TAG);
					dictReplaces.add(t.getText() + EXCEL_SPLIT_TAG + val);
				}else{
					dictReplaces.add(t.getText() + EXCEL_SPLIT_TAG + t.getValue());
				}
			}
		}
		if (dictReplaces != null && dictReplaces.size() != 0) {
			log.info("---AutoPoi--Get_DB_Dict------"+ dictReplaces.toString());
			return dictReplaces.toArray(new String[dictReplaces.size()]);
		}
		return null;
	}
}
