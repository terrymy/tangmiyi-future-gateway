package com.tangmiyi.future.gateway.controller;

import com.tangmiyi.future.gateway.pojo.GatewayRouteDefinition;
import com.tangmiyi.future.gateway.service.GatewayRoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gateway/routes")
public class GatewayRoutesController {

    @Autowired
    private GatewayRoutesService routesService;

    @Value("${request.client.checkFlag:pmckdmwekmk}")
    private String requestCheckFlag;

    private Boolean checkFlag(String checkFlag){
        if(requestCheckFlag.equals(checkFlag)){
            return true;
        }
        return false;
    }

    /**
     * 获取所有动态路由信息
     * @return
     */
    @RequestMapping("/gatewayService")
    public List<GatewayRouteDefinition> getRouteDefinitions(@RequestParam(value = "gatewayService") String gatewayService,
                                                            @RequestParam(value = "checkFlag") String checkFlag){
        if(!checkFlag(checkFlag)){
            return null;
        }
        return routesService.getRouteDefinitions(gatewayService);
    }

    /**
     * 添加路由信息
     * @param route
     * @return
     */
    @RequestMapping(value = "/add" , method = RequestMethod.POST)
    public String add(@RequestBody GatewayRouteDefinition route,@RequestParam(value = "checkFlag") String checkFlag){
        if(!checkFlag(checkFlag)){
            return null;
        }
        return routesService.add(route) > 0 ? "success" : "fail";
    }

    /**
     * 更新路由
     * @param route
     * @return
     */
    @RequestMapping(value = "/update" , method = RequestMethod.POST)
    public String update(@RequestBody GatewayRouteDefinition route,@RequestParam(value = "checkFlag") String checkFlag){
        if(!checkFlag(checkFlag)){
            return null;
        }
        return routesService.update(route) > 0 ? "success" : "fail";
    }

    @RequestMapping("/delete")
    public String delete(@RequestParam(value = "checkFlag") String checkFlag,@RequestParam("routeId") String routeId){
        if(!checkFlag(checkFlag)){
            return null;
        }
        routesService.delete(routeId);
        return "delSuccess";
    }
}
