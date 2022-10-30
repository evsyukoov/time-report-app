package ru.evsyukov.polling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TelegramPollingApp {

    public static void main(String[] args) {
        try {
            SpringApplication.run(TelegramPollingApp.class);
        } catch (Exception e) {
            log.error("Failed to start polling-bot module: ", e);
        }
    }
}
