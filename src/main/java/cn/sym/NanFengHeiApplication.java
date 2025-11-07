package cn.sym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 * 
 * @author user
 */
@SpringBootApplication
@EnableScheduling
public class NanFengHeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NanFengHeiApplication.class, args);
    }
}