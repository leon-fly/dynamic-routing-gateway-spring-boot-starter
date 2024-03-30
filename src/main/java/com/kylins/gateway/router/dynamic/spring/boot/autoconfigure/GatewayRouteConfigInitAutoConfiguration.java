package com.kylins.gateway.router.dynamic.spring.boot.autoconfigure;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kylins.gateway.router.dynamic.spring.boot.autoconfigure.specification.DynamicRouteService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: LeonWang
 * Date: 2024/3/27
 * Description: Initiate the dynamic routing config and add the listener of routing configuration change
 */
@Component
@RefreshScope
@Data
@Slf4j
@ConditionalOnProperty(name = "gateway.routes.config.useDynamicRoutes", havingValue = "true")
public class GatewayRouteConfigInitAutoConfiguration {
    @Autowired
    private GatewayRouteConfigProperties routeConfigProperties;
    @Autowired
    private NacosConfigProperties nacosConfigProperties;
    @Autowired
    private DynamicRouteService dynamicRouteService;
    @Autowired
    private ConfigService configService;

    @PostConstruct
    public void init() {
        log.info("initiate the dynamic routing configuration and listener....");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String initConfigInfo = configService.getConfigAndSignListener(routeConfigProperties.getDataId(),
                    routeConfigProperties.getGroup(), nacosConfigProperties.getTimeout(), new NacosRoutingConfigurationListener(dynamicRouteService));
            log.info("init current routing configuration : \r\n{}", initConfigInfo);
            if (StringUtils.hasLength(initConfigInfo)) {
                List<RouteDefinition> routeDefinitions = objectMapper.readValue(initConfigInfo, new TypeReference<>() {
                });
                dynamicRouteService.add(routeDefinitions, true);
            } else {
                log.warn("current routing configuration is empty");
            }
            log.info("the dynamic routing configuration initiated and listener added");
        } catch (Exception e) {
            log.error("routing configuration initiation error", e);
        }
    }
}
