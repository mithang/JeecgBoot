package org.jeecg.modules.airag.wordtpl.dto;

import java.util.List;

import lombok.Data;

@Data
public class WordTableRowDTO {

	/**  
	 * @Fields height : Chiều cao hàng
	 * @author chenrui
	 * @date 2024-09-26 09:45:30 
	 */  
	private Integer height;
	
	/**  
	 * @Fields minHeight : Chiều cao tối thiểu của hàng
	 * @author chenrui
	 * @date 2024-09-26 09:47:28 
	 */  
	private int minHeight = 42;
	
	/**  
	 * @Fields tdList : Dữ liệu hàng
	 * @author chenrui
	 * @date 2024-09-26 09:46:02 
	 */  
	private List<WordTableCellDTO> tdList;
}
