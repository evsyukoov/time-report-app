package ru.evsyukov.polling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Slf4j
@EntityScan(value = {"ru.evsyukov.app.data.entity"})
@EnableJpaRepositories(value = {"ru.evsyukov.app.data.repository"})
public class TelegramPollingApp {

    public static void main(String[] args) {
        try {
            SpringApplication.run(TelegramPollingApp.class);
        } catch (Exception e) {
            log.error("Failed to start polling-bot module: ", e);
        }
    }
}
