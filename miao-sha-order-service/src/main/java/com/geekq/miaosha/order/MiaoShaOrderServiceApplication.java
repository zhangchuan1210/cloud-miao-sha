package com.geekq.miaosha.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(scanBasePackages = "com.geekq.miaosha")
@MapperScan("com.geekq.miaosha.order.mapper,com.geekq.miaosha.common.biz.mapper")
@EnableEurekaClient
@EnableRedisHttpSession
public class MiaoShaOrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiaoShaOrderServiceApplication.class,args);
        System.out.println("");
    }
}
