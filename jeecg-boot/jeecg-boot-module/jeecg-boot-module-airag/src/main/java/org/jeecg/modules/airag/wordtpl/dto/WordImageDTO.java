package org.jeecg.modules.airag.wordtpl.dto;

import lombok.Data;

/**  
 * @ClassName: DocImageDto
 * @Description: word Lớp thực thể cho hình ảnh trong tài liệu
 * @author chenrui
 * @date 2024-10-02 09:17:59 
*/ 
@Data
public class WordImageDTO {

	/**  
	 * @Fields type : Loại
	 * @author chenrui
	 * @date 2024-09-29 08:53:27 
	 */  
	private String type = "image";
	/**  
	 * @Fields value : Nội dung
	 * @author chenrui
	 * @date 2024-09-24 10:20:12 
	 */  
	private String value = "";
	
	/**  
	 * @Fields width : Chiều rộng hình ảnh
	 * @author chenrui
	 * @date 2024-10-02 09:22:33 
	 */  
	private double width;
	
	/**  
	 * @Fields height : Chiều cao hình ảnh
	 * @author chenrui
	 * @date 2024-10-02 09:22:40 
	 */  
	private double height;
	
	/**  
	 * @Fields rowFlex : Kiểu căn ngang, mặc định left
	 * @author chenrui
	 * @date 2024-09-27 09:12:18 
	 */  
	private String rowFlex = "left";
}
