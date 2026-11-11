package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Thông tin kết quả đồng bộ, bao gồm thông tin thành công và thông tin thất bại
 *
 * @author sunjianlei
 */
@Data
public class SyncInfoVo {

    /**
     * Thông tin thành công
     */
    private List<String> successInfo;
    /**
     * Thông tin thất bại
     */
    private List<String> failInfo;

    public SyncInfoVo() {
        this.successInfo = new ArrayList<>();
        this.failInfo = new ArrayList<>();
    }

    public SyncInfoVo(List<String> successInfo, List<String> failInfo) {
        this.successInfo = successInfo;
        this.failInfo = failInfo;
    }

    public SyncInfoVo addSuccessInfo(String info) {
        this.successInfo.add(info);
        return this;
    }

    public SyncInfoVo addFailInfo(String info) {
        this.failInfo.add(info);
        return this;
    }
}
