package org.jeecg.modules.airag.wordtpl.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class WordTableCellDTO {

	/**  
	 * @Fields colspan : Số cột gộp
	 * @author chenrui
	 * @date 2024-09-26 09:37:27 
	 */  
	private int colspan;
	
	/**  
	 * @Fields rowspan : Số hàng gộp
	 * @author chenrui
	 * @date 2024-09-26 09:38:22 
	 */  
	private int rowspan;
	
	/**  
	 * @Fields value : Dữ liệu ô
	 * @author chenrui
	 * @date 2024-09-26 09:42:14 
	 */  
	private List<Object> value = new ArrayList<>();
	
	/**  
	 * @Fields verticalAlign : Cách căn dọc, mặc định top
	 * @author chenrui
	 * @date 2024-09-27 09:16:56 
	 */  
	private String verticalAlign = "top";
	
	/**  
	 * @Fields backgroundColor : Màu nền
	 * @author chenrui
	 * @date 2024-11-18 09:56:28 
	 */  
	private String backgroundColor;
}
