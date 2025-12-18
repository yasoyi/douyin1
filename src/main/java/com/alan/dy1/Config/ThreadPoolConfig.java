package com.alan.dy1.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
@Configuration
public class ThreadPoolConfig {
    //查询作品数量专属线程池
    @Bean("getWorkCountExecutor")
    public Executor getWorkCountExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("getWorkCount");
        executor.initialize();
        return executor;
    }

    //下载音频并转换为WAV专属线程池
    @Bean("downloadAudioExecutor")
    public Executor downloadAudioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("downloadAudio");
        executor.initialize();
        return executor;
    }

    //百度api转文字专属线程池
    @Bean("baiduApiExecutor")
    public Executor baiduApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("baiduApi");
        executor.initialize();
        return executor;
    }

    //大模型文字提取关键词+选股专属线程池
    @Bean("textExtractAndBuyExecutor")
    public Executor textExtractExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("textExtract");
        executor.initialize();
        return executor;
    }



}
