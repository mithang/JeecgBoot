package org.jeecg.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Tham số đầu vào khi gọi quy trình AI
 * for [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
 * @author chenrui
 * @date 2025/9/2 14:11
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiragFlowDTO implements Serializable {


    private static final long serialVersionUID = 7431775881170684867L;

    /**
     * Quy trình id
     */
    private String flowId;


    /**
     * Tham số đầu vào
     */
    private Map<String, Object> inputParams;

    /**
     * Có trả về dạng streaming hay không
     */
    private boolean isStream;

}
