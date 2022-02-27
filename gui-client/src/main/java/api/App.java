package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("hibernate.entities")
public class App {

    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
