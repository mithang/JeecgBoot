package org.jeecg.modules.airag.prompts.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: AiragDebugVo
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Data
public class AiragDebugVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Prompt
     */
    private String prompts;
    /**
     * Nội dung nhập
     */
    private String content;
	/**Mô hình lớn tương thích ID*/
    private String modelId;
	/**Cấu hình tham số của mô hình lớn*/
    private String modelParam;
}
