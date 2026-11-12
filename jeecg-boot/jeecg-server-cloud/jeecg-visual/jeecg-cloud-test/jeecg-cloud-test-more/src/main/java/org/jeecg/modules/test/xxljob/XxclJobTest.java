
package org.jeecg.modules.test.xxljob;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.tool.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *  xxl-job Kiểm thử tác vụ định kỳ
 * @author: zyf
 * @date: 2022/04/21
 */
@Component
@Slf4j
public class XxclJobTest {


    /**
     * Tác vụ đơn giản
     *
     * @param params
     * @return
     */

    @XxlJob(value = "xxclJobTest")
    public Response<String> demoJobHandler(String params) {
        log.info("Tôi là jeecg-system Tác vụ định kỳ trong dịch vụ xxclJobTest , Tôi đã thực thi...............................");
        return Response.ofSuccess();
    }

    public void init() {
        log.info("init");
    }

    public void destroy() {
        log.info("destory");
    }

}

