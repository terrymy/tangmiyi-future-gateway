package com.tangmiyi.future.gateway.config;

import com.tangmiyi.future.gateway.pojo.GatewayRouteDefinition;
import com.tangmiyi.future.gateway.router.GatewayRouteListSingle;
import com.tangmiyi.future.gateway.service.GatewayRoutesService;
import com.tangmiyi.future.gateway.service.impl.DynamicRouteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 初始化加载系统路由信息
 */
@Component
@Order(1)
public class GatewayRouteslConfig implements CommandLineRunner {

    @Autowired
    private GatewayRoutesService gatewayRoutesService;

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    @Value("${spring.profiles.active}")
    private String active;

    @Override
    public void run(String... args) {
        GatewayRouteListSingle gatewayRouteListSingle = GatewayRouteListSingle.getInstance();
        List<GatewayRouteDefinition> lasts = gatewayRoutesService.getRouteDefinitions(active);
        for(GatewayRouteDefinition definition : lasts){
            // 新增路由
            RouteDefinition routeDefinition = gatewayRoutesService.assembleRouteDefinition(definition);
            dynamicRouteService.add(routeDefinition);
        }
        gatewayRouteListSingle.setGatewayRouteDefinitionList(lasts);
    }
}
