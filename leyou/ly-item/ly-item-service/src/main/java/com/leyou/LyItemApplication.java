package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.leyou.item.mapper")  // mapper接口的包扫描
public class LyItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyItemApplication.class);
    }

}
