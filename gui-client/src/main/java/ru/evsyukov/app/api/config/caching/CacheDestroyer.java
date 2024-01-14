package ru.evsyukov.app.api.config.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class CacheDestroyer {

    @CacheEvict(value = {"projects", "employees", "employeeNames"}, allEntries = true)
    @Scheduled(fixedRate = 60 * 10 * 1000) //10 минут
    public void destroyCache() {
        log.info("Destroy all caches");
    }


}
