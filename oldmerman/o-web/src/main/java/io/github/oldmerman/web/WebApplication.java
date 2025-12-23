package io.github.oldmerman.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
@MapperScan("io.github.oldmerman.web.mapper")
@ComponentScan({"io.github.oldmerman.web", "io.github.oldmerman.common"})
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class);
    }
}
