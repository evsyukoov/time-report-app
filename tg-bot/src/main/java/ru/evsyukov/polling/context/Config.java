package ru.evsyukov.polling.context;

import com.ibm.icu.text.Transliterator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    @Bean
    List<String> cacheProjects() {
        return new CopyOnWriteArrayList<>();
    }

    @Bean
    Transliterator latinToCyrillic() {
        return Transliterator.getInstance("Latin-Russian/BGN");
    }

    @Bean
    Transliterator cyrillicToLatin() {
        return Transliterator.getInstance("Russian-Latin/BGN");
    }


}
