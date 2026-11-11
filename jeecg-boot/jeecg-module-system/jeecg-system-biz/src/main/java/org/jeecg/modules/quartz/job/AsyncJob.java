package org.jeecg.modules.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.quartz.*;

/**
 * @Description: Kiểm tra tác vụ định kỳ đồng bộ
 *
 * Đồng bộ ở đây nghĩa là khi thời gian thực thi của tác vụ định giờ lớn hơn khoảng thời gian của tác vụ
 * Sẽ đợi nhiệm vụ đầu tiên thực thi xong mới đi đến nhiệm vụ thứ hai
 *
 *
 * @author: taoyan
 * @date: 2020 năm 06 tháng 19 ngày
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class AsyncJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info(" --- Bắt đầu lập lịch tác vụ đồng bộ --- ");
        try {
            //Tại đây mô phỏng thời gian thực thi tác vụ là 5 giây, biểu thức tác vụ được cấu hình thực thi mỗi giây một lần:0/1 * * * * ? *
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Qua kiểm tra, cứ mỗi 5 giây thực thi một lần
        log.info(" --- Thực thi xong, thời gian:"+DateUtils.now()+"---");
    }

}
