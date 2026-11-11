package org.jeecg.modules.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.message.entity.SysMessageTemplate;

import java.util.List;

/**
 * @Description: Mẫu tin nhắn
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
public interface SysMessageTemplateMapper extends BaseMapper<SysMessageTemplate> {

    /**
     * Truy vấn mẫu tin nhắn qua CODE mẫu
     * @param code Mẫu CODE
     * @return List<SysMessageTemplate>
     */
    @Select("SELECT * FROM SYS_SMS_TEMPLATE WHERE TEMPLATE_CODE = #{code}")
    List<SysMessageTemplate> selectByCode(String code);
}
