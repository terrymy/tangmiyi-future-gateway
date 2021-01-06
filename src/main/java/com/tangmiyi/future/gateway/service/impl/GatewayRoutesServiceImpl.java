package com.tangmiyi.future.gateway.service.impl;

import com.alibaba.fastjson.JSON;
import com.tangmiyi.future.gateway.dao.GatewayRoutesMapper;
import com.tangmiyi.future.gateway.pojo.GatewayFilterDefinition;
import com.tangmiyi.future.gateway.pojo.GatewayPredicateDefinition;
import com.tangmiyi.future.gateway.pojo.GatewayRouteDefinition;
import com.tangmiyi.future.gateway.pojo.GatewayRoutesDO;
import com.tangmiyi.future.gateway.service.GatewayRoutesService;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayRoutesServiceImpl implements GatewayRoutesService {

    @Resource
    private GatewayRoutesMapper gatewayRoutesMapper;

    private GatewayRoutesDO dtoToDO(GatewayRouteDefinition gatewayRouteDefinition){
        GatewayRoutesDO gatewayRoutesEntity = new GatewayRoutesDO();
        gatewayRoutesEntity.setRouteOrder(gatewayRouteDefinition.getOrder());
        gatewayRoutesEntity.setRouteId(gatewayRouteDefinition.getId());
        gatewayRoutesEntity.setFilters(JSON.toJSONString(gatewayRouteDefinition.getFilters()));
        gatewayRoutesEntity.setRouteUri(gatewayRouteDefinition.getUri());
        gatewayRoutesEntity.setPredicates(JSON.toJSONString(gatewayRouteDefinition.getPredicates()));
        gatewayRoutesEntity.setGatewayService(gatewayRouteDefinition.getGatewayService());
        gatewayRoutesEntity.setRouteVersion(gatewayRouteDefinition.getRouteVersion());
        return gatewayRoutesEntity;
    }


    @Override
    public int add(GatewayRouteDefinition route) {
        return gatewayRoutesMapper.insertSelective(dtoToDO(route));
    }

    @Override
    public int update(GatewayRouteDefinition route) {
        return gatewayRoutesMapper.updateByRouteId(dtoToDO(route));
    }

    @Override
    public int delete(String routeId) {
        return gatewayRoutesMapper.deleteByRouteId(routeId);
    }

    /**
     * 查询路由信息
     * @return
     */
    @Override
    public List<GatewayRoutesDO> getRoutesByService(String gatewayService) {
        return gatewayRoutesMapper.selectByService(gatewayService);
    }

    /**
     * 返回组装后网关需要的路由信息
     * @return
     */
    @Override
    public List<GatewayRouteDefinition> getRouteDefinitions(String gatewayService) {
        List<GatewayRouteDefinition> routeDefinitions = new ArrayList<>();
        List<GatewayRoutesDO> routes = getRoutesByService(gatewayService);
        for(GatewayRoutesDO gatewayRoute : routes){
            GatewayRouteDefinition routeDefinition = new GatewayRouteDefinition();
            routeDefinition.setId(gatewayRoute.getRouteId());
            routeDefinition.setUri(gatewayRoute.getRouteUri());
            routeDefinition.setFilters(gatewayRoute.getFilterDefinition());
            routeDefinition.setPredicates(gatewayRoute.getPredicateDefinition());
            routeDefinition.setGatewayService(gatewayRoute.getGatewayService());
            routeDefinition.setRouteVersion(gatewayRoute.getRouteVersion());
            routeDefinitions.add(routeDefinition);
        }
        return routeDefinitions;
    }

    /**
     * 参数转换成路由对象
     */
    @Override
    public RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gwdefinition) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(gwdefinition.getId());
        definition.setOrder(gwdefinition.getOrder());
        //设置断言
        List<PredicateDefinition> pdList = new ArrayList<>();
        List<GatewayPredicateDefinition> gatewayPredicateDefinitionList=gwdefinition.getPredicates();
        for (GatewayPredicateDefinition gpDefinition: gatewayPredicateDefinitionList) {
            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setArgs(gpDefinition.getArgs());
            predicate.setName(gpDefinition.getName());
            pdList.add(predicate);
        }
        definition.setPredicates(pdList);
        //设置过滤器
        List<FilterDefinition> filters = new ArrayList();
        List<GatewayFilterDefinition> gatewayFilters = gwdefinition.getFilters();
        for(GatewayFilterDefinition filterDefinition : gatewayFilters){
            FilterDefinition filter = new FilterDefinition();
            filter.setName(filterDefinition.getName());
            filter.setArgs(filterDefinition.getArgs());
            filters.add(filter);
        }
        definition.setFilters(filters);
        URI uri = null;
        if(gwdefinition.getUri().startsWith("http")){
            uri = UriComponentsBuilder.fromHttpUrl(gwdefinition.getUri()).build().toUri();
        }else{
            uri = URI.create(gwdefinition.getUri());
        }
        definition.setUri(uri);
        return definition;
    }
}
