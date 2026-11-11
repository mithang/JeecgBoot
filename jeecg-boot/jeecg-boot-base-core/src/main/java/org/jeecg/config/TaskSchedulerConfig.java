package org.jeecg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Cấu hình bộ lập lịch tác vụ
 * Cung cấp Bean ThreadPoolTaskScheduler dùng cho lập lịch quy trình AI RAG và các chức năng khác
 * Chỉ tạo khi trong container không tồn tại ThreadPoolTaskScheduler
 *
 * @author jeecg
 */
@Slf4j
@Configuration
public class TaskSchedulerConfig {

    @Bean
    @ConditionalOnMissingBean(ThreadPoolTaskScheduler.class)
    public ThreadPoolTaskScheduler taskScheduler() {
        log.info("Khởi tạo bộ lập lịch tác vụ định kỳ ThreadPoolTaskScheduler");

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("airag-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}
