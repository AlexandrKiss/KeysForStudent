package ua.kiev.prog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class KeysApplication {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(KeysApplication.class, args);

    }
}
