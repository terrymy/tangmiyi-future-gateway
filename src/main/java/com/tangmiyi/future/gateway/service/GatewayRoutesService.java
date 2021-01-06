package com.tangmiyi.future.gateway.service;

import com.tangmiyi.future.gateway.pojo.GatewayRouteDefinition;
import com.tangmiyi.future.gateway.pojo.GatewayRoutesDO;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;

public interface GatewayRoutesService {

    int add(GatewayRouteDefinition route);

    int update(GatewayRouteDefinition route);

    int delete(String routeId);

    /**
     * 查询路由信息
     * @return
     */
    List<GatewayRoutesDO> getRoutesByService(String gatewayService);

    /**
     * 返回组装后网关需要的路由信息
     * @return
     */
    List<GatewayRouteDefinition> getRouteDefinitions(String gatewayService);

    RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gwdefinition);
}
