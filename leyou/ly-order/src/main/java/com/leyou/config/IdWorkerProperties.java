package com.leyou.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自定义的idWorker的配置类
 */
@ConfigurationProperties(prefix = "ly.worker")
@Getter
@Setter
public class IdWorkerProperties {

    private long workerId;// 当前机器id
    private long datacenterId;// 序列号

}
