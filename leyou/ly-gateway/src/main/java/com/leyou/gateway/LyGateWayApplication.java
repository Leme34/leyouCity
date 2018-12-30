package com.leyou.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringCloudApplication
public class LyGateWayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyGateWayApplication.class);
    }

}
