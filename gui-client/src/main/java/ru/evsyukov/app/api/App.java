package ru.evsyukov.app.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(value = {"ru.evsyukov.app.data.entity"})
@EnableJpaRepositories(value = {"ru.evsyukov.app.data.repository"})
public class App {

    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
