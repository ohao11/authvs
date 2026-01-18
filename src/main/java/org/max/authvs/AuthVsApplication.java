package org.max.authvs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("org.max.authvs.mapper")
public class AuthVsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthVsApplication.class, args);
    }
}