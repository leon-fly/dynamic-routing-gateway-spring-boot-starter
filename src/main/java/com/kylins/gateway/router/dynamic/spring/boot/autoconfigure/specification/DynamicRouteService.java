package com.kylins.gateway.router.dynamic.spring.boot.autoconfigure.specification;

import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;

/**
 * Author: LeonWang
 * Date: 2024/3/27
 * Description: service for operation of the spring cloud gateway routing.
 * the operation like update / delete / add can be refreshed automatically only if you set the param autoRefreshRoutes=true, or else it will not effective
 */
public interface DynamicRouteService {
    List<RouteDefinition> getCurrentRouteDefinitions();
    void update(List<RouteDefinition> routeDefinitions, boolean autoRefreshRoutes);
    void delete(List<String> routeId, boolean autoRefreshRoutes);
    void add(List<RouteDefinition> routeDefinitions, boolean autoRefreshRoutes);
    void refreshRoutes();
}



