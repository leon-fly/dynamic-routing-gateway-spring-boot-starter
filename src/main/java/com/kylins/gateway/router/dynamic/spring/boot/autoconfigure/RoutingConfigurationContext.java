package com.kylins.gateway.router.dynamic.spring.boot.autoconfigure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kylins.gateway.router.dynamic.spring.boot.autoconfigure.specification.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: LeonWang
 * Date: 2024/3/30
 * Description: a context of routing, it owns the current routes info
 * and the latest routs config which should be changed but is not effective.
 * the goal of this class is updating the routes.
 */
@Slf4j
public class RoutingConfigurationContext {
    private final DynamicRouteService routeService;
    private final String configInfo;
    private List<RouteDefinition> tobeAddedRoutes;
    private List<RouteDefinition> tobeUpdatedRoutes;
    private List<String> tobeDeletedRouteIds;

    public RoutingConfigurationContext(DynamicRouteService routeService, String configInfo) {
        this.routeService = routeService;
        this.configInfo = configInfo;
    }

    private void findChanges() {
        List<RouteDefinition> currentRouteDefinitions = routeService.getCurrentRouteDefinitions();
        log.info("current routes : {}", currentRouteDefinitions);
        boolean hasRoutesCurrently = !CollectionUtils.isEmpty(currentRouteDefinitions);

        List<RouteDefinition> newRouteDefinitions = Collections.emptyList();
        if (StringUtils.hasLength(configInfo)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                newRouteDefinitions = objectMapper.readValue(configInfo, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                log.error("routing configuration parsed error : {}", e.getMessage(), e);
                return;
            }
        }

        tobeAddedRoutes = new ArrayList<>();
        tobeUpdatedRoutes = new ArrayList<>();
        tobeDeletedRouteIds = new ArrayList<>();

        Map<String, RouteDefinition> currentRoutesMap = currentRouteDefinitions.stream()
                .collect(Collectors.toMap(RouteDefinition::getId, routeDefinition -> routeDefinition));
        Map<String, RouteDefinition> newRoutesMap = newRouteDefinitions.stream()
                .collect(Collectors.toMap(RouteDefinition::getId, routeDefinition -> routeDefinition));
        if (hasRoutesCurrently) {
            currentRouteDefinitions.forEach(routeDefinition -> {
                String routeId = routeDefinition.getId();
                boolean isDeleted = !newRoutesMap.containsKey(routeId);
                if (isDeleted) {
                    tobeDeletedRouteIds.add(routeId);
                }
                if (!isDeleted && !currentRoutesMap.get(routeId).equals(newRoutesMap.get(routeId))) {
                    tobeUpdatedRoutes.add(newRoutesMap.get(routeId));
                }
            });
        }
        tobeAddedRoutes = newRouteDefinitions.stream().filter(
                routeDefinition -> !currentRoutesMap.containsKey(routeDefinition.getId())).collect(Collectors.toList());
    }

    public void updateRouting() {
        findChanges();
        log.info("routes changes, added count [{}] : {} , updated count [{}] : {} ,deleted count [{}]: {}",
                tobeAddedRoutes.size(), tobeAddedRoutes, tobeUpdatedRoutes.size(), tobeUpdatedRoutes, tobeDeletedRouteIds.size(), tobeDeletedRouteIds);
        routeService.add(tobeAddedRoutes, false);
        routeService.update(tobeUpdatedRoutes, false);
        routeService.delete(tobeDeletedRouteIds, false);
        routeService.refreshRoutes();
        log.info("routes updated");
    }
}
