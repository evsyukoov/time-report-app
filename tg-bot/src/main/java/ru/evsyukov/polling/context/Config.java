package ru.evsyukov.polling.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class Config {

    @Bean
    ThreadPoolTaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor threadPoolExecutor =  new ThreadPoolTaskExecutor();
        threadPoolExecutor.setCorePoolSize(10);
        threadPoolExecutor.setMaxPoolSize(100);
        threadPoolExecutor.setKeepAliveSeconds(120);
        threadPoolExecutor.setQueueCapacity(100);
        return threadPoolExecutor;
    }
}
