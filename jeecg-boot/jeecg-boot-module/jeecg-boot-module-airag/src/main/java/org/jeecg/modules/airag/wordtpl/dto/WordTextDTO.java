package org.jeecg.modules.airag.wordtpl.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @author chenrui
 * @ClassName: DocTextDto
 * @Description: word Lớp thực thể văn bản
 * @date 2024-09-24 10:19:57
 */
@Data
public class WordTextDTO {

    /**
     * @Fields type : Loại
     * @author chenrui
     * @date 2024-09-29 08:53:27
     */
    private String type;
    /**
     * @Fields value : Nội dung
     * @author chenrui
     * @date 2024-09-24 10:20:12
     */
    private String value = "";

    /**
     * @Fields bold : Có in đậm hay không, mặc định false
     * @author chenrui
     * @date 2024-09-24 10:20:33
     */
    private boolean bold = false;

    /**
     * @Fields color : Màu chữ
     * @author chenrui
     * @date 2024-09-24 10:21:08
     */
    private String color;

    /**
     * @Fields italic : Có in nghiêng hay không, mặc định false
     * @author chenrui
     * @date 2024-09-24 10:21:25
     */
    private boolean italic = false;

    /**
     * @Fields underline : Có gạch chân hay không, mặc định false
     * @author chenrui
     * @date 2024-09-24 10:21:47
     */
    private boolean underline = false;

    /**
     * @Fields strikeout : Gạch ngang, mặc định false
     * @author chenrui
     * @date 2024-09-24 10:22:06
     */
    private boolean strikeout = false;

    /**
     * @Fields size : Cỡ chữ
     * @author chenrui
     * @date 2024-09-24 10:44:42
     */
    private int size;

    /**
     * @Fields font : Phông chữ, mặc định Microsoft YaHei
     * @author chenrui
     * @date 2024-09-24 10:45:31
     */
    private String font = "微软雅黑";

    /**
     * @Fields highlight : Màu nổi bật
     * @author chenrui
     * @date 2024-09-25 11:20:23
     */
    private String highlight;

    /**
     * @Fields rowFlex : Kiểu căn ngang, mặc định left
     * @author chenrui
     * @date 2024-09-27 09:12:18
     */
    private String rowFlex = "left";

    private List<Object> dashArray = new ArrayList<>();
}
