package ru.evsyukov.app.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(value = {"ru.evsyukov.app.data.entity"})
@EnableJpaRepositories(value = {"ru.evsyukov.app.data.repository"})
@Slf4j
public class App {

    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class);
            log.info("Successfully start spring-boot app");
        } catch (Exception e) {
           log.error("Fatal error spring-boot app started", e);
        }
    }
}
