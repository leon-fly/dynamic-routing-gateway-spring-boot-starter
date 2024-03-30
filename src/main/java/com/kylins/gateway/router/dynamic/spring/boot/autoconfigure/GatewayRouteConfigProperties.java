package com.kylins.gateway.router.dynamic.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Author: LeonWang
 * Date: 2024/3/27
 * Description: gateway route related configuration properties
 */

@ConfigurationProperties(prefix = "gateway.routes.config")
@Configuration
@Data
public class GatewayRouteConfigProperties {
    private boolean useDynamicRoutes = false;
    private String dataId;
    private String group;
    private String namespace;
}
