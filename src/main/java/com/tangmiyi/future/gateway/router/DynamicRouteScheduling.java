package com.tangmiyi.future.gateway.router;

import com.tangmiyi.future.gateway.pojo.GatewayRouteDefinition;
import com.tangmiyi.future.gateway.service.GatewayRoutesService;
import com.tangmiyi.future.gateway.service.impl.DynamicRouteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务，拉取路由信息
 */
@Component
public class DynamicRouteScheduling {

    @Autowired
    private GatewayRoutesService gatewayRoutesService;

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    @Value("${spring.profiles.active}")
    private String active;

    /**
     * 每60秒执行一次,如果版本号不相等则获取最新路由信息并更新网关路由
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void getDynamicRouteInfo(){
        // 先拉取版本信息，如果版本号不想等则更新路由
        List<GatewayRouteDefinition> lasts = gatewayRoutesService.getRouteDefinitions(active);
        GatewayRouteListSingle gatewayRouteListSingle = GatewayRouteListSingle.getInstance();
        List<GatewayRouteDefinition> currents = gatewayRouteListSingle.getGatewayRouteDefinitionList();
        // 取差集lasts-currents
        List<GatewayRouteDefinition> adds = lasts.stream().filter(sub->{
            return currents.stream().filter(all -> {
                return sub.getId().equalsIgnoreCase(all.getId());
            }).count() == 0;
        }).collect(Collectors.toList());

        // 取差集currents-lasts
        List<GatewayRouteDefinition> deletes = currents.stream().filter(sub->{
            return lasts.stream().filter(all -> {
                return sub.getId().equalsIgnoreCase(all.getId());
            }).count() == 0;
        }).collect(Collectors.toList());

        // 取版本号不一致的交集
        List<GatewayRouteDefinition> updates = lasts.stream().filter(sub->{
            return currents.stream().filter(all -> {
                return sub.getId().equalsIgnoreCase(all.getId()) && all.getRouteVersion().intValue() < sub.getRouteVersion().intValue();
            }).count() > 0;
        }).collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(updates)){
            for(GatewayRouteDefinition definition : updates){
                // 更新路由
                RouteDefinition routeDefinition = gatewayRoutesService.assembleRouteDefinition(definition);
                dynamicRouteService.update(routeDefinition);
            }
        }

        if(!CollectionUtils.isEmpty(adds)){
            for(GatewayRouteDefinition definition : adds){
                // 新增路由
                RouteDefinition routeDefinition = gatewayRoutesService.assembleRouteDefinition(definition);
                dynamicRouteService.add(routeDefinition);
            }
        }

        if(!CollectionUtils.isEmpty(deletes)){
            for(GatewayRouteDefinition definition : deletes){
                // 删除路由
                RouteDefinition routeDefinition = gatewayRoutesService.assembleRouteDefinition(definition);
                dynamicRouteService.delete(routeDefinition.getId());
            }
        }

        // 更新单例路由列表
        if(!CollectionUtils.isEmpty(deletes) || !CollectionUtils.isEmpty(adds) || !CollectionUtils.isEmpty(updates)){
            gatewayRouteListSingle.setGatewayRouteDefinitionList(lasts);
        }
    }
}
