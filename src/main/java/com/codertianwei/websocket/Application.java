package com.codertianwei.websocket;

import com.codertianwei.websocket.websocket.WebsocketServer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ComponentScan({"com.codertianwei.websocket", "${package}"})
@EnableAsync
@EnableAutoConfiguration
@EnableWebMvc
@PropertySource({"classpath:application-${env}.properties"})
@SpringBootApplication
public class Application {
    private static Application instance;

    @Autowired
    @Getter
    private ApplicationContext applicationContext;

    public static Application getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            instance = this;
            ctx.getBean(WebsocketServer.class).start();
        };
    }
}
