package com.kylins.gateway.router.dynamic.spring.boot.autoconfigure;

import com.alibaba.nacos.api.config.listener.Listener;
import com.kylins.gateway.router.dynamic.spring.boot.autoconfigure.specification.DynamicRouteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

/**
 * Author: LeonWang
 * Date: 2024/3/27
 * Description: listen the change of routing configuration in nacos
 */
@Slf4j
@AllArgsConstructor
public class NacosRoutingConfigurationListener implements Listener {
    private DynamicRouteService routeService;

    @Override
    public Executor getExecutor() {
        return null; //use nacos inner Executor
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("[routing listener] routing config changed ：\r\n{}", configInfo);
        RoutingConfigurationContext routingConfigurationContext = new RoutingConfigurationContext(routeService, configInfo);
        routingConfigurationContext.updateRouting();
    }
}
