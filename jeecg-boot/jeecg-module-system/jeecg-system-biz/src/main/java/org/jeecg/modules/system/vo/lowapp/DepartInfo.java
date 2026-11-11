package org.jeecg.modules.system.vo.lowapp;

import lombok.Data;

import java.util.List;

/**
 * @Author taoYan
 * @Date 2022/12/30 10:52
 **/
@Data
public class DepartInfo {

    private String id;

    /**
     * Tên cấp trên-Tên cấp dưới
     */
    private List<String> orgName;

    /**
     * Cấp trên ID-Cấp dưới ID
     */
    private List<String> orgId;

}
