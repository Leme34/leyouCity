package com.leyou.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 过滤白名单配置类
 */
@ConfigurationProperties(prefix = "ly.filter")
@Component
@Getter
@Setter
public class AllowProperties {

    private List<String> allowPaths;

}
