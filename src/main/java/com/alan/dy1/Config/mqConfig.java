package com.alan.dy1.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class mqConfig {
    // 队列1：获取作品数量任务结果 → 下载音频任务
    public static final String QUEUE_NEW_TO_DownloadAndChange = "queue.new.to.downloadAndChange";
    // 队列2：音频任务结果 → C任务
    public static final String QUEUE_DownloadAndChange_TO_BaiduApi = "queue.downloadAndChange.to.baiduApi";

    @Bean
    public Queue queueA() {
        return new Queue(QUEUE_NEW_TO_DownloadAndChange);
    }

    @Bean
    public Queue queueB() {
        return new Queue(QUEUE_DownloadAndChange_TO_BaiduApi);
    }
}
