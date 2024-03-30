package com.kylins.gateway.router.dynamic.spring.boot.autoconfigure;

import com.kylins.gateway.router.dynamic.spring.boot.autoconfigure.specification.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: LeonWang
 * Date: 2024/3/27
 * Description:
 */
@ConditionalOnProperty(name = "gateway.routes.config.useDynamicRoutes", havingValue = "true")
@Service
@Slf4j
public class DynamicRouteServiceImplAutoConfiguration implements DynamicRouteService, ApplicationEventPublisherAware {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public List<RouteDefinition> getCurrentRouteDefinitions() {
        Flux<RouteDefinition> routeDefinitionFlux = routeDefinitionLocator.getRouteDefinitions();
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        routeDefinitionFlux.subscribe(routeDefinitions::add);
        return routeDefinitions;
    }

    @Override
    public void update(List<RouteDefinition> routeDefinitions, boolean autoRefreshRoutes) {
        log.info("update the routes : {}", routeDefinitions);
        if (CollectionUtils.isEmpty(routeDefinitions)) {
            return;
        }
        routeDefinitions.forEach(routeDefinition -> {
            routeDefinitionWriter.delete(Mono.just(routeDefinition.getId())).subscribe();
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        });
        if (autoRefreshRoutes) {
            refreshRoutes();
        }
    }

    @Override
    public void delete(List<String> routeIds, boolean autoRefreshRoutes) {
        log.info("delete these routes : {}", routeIds);
        if (CollectionUtils.isEmpty(routeIds)) {
            return;
        }
        routeIds.forEach(routeId -> routeDefinitionWriter.delete(Mono.just(routeId)).subscribe());
        if (autoRefreshRoutes) {
            refreshRoutes();
        }
    }

    @Override
    public void add(List<RouteDefinition> routeDefinitions, boolean autoRefreshRoutes) {
        log.info("add new routes: {}", routeDefinitions);
        if (CollectionUtils.isEmpty(routeDefinitions)) {
            return;
        }
        routeDefinitions.forEach(routeDefinition -> {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        });
        if (autoRefreshRoutes) {
            refreshRoutes();
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Override
    public void refreshRoutes() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
