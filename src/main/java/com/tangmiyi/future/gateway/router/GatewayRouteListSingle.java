package com.tangmiyi.future.gateway.router;

import com.tangmiyi.future.gateway.pojo.GatewayRouteDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @ClassName: GatewayRouteListSingle
 * @Description: 应用启动加载路由列表，单例模式
 */
@Setter
@Getter
public class GatewayRouteListSingle {

    /**
     * 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载，volatile防止指令重排序，在instance未被new时就被返回
     */
    private static volatile GatewayRouteListSingle instance = null;

    /**
     * 路由列表
     */
    private List<GatewayRouteDefinition> gatewayRouteDefinitionList;

    private GatewayRouteListSingle() {

    }

    public static synchronized GatewayRouteListSingle getInstance() {
        if (instance == null) {
            synchronized(GatewayRouteListSingle.class){
                if(instance == null){
                    instance = new GatewayRouteListSingle();
                }
            }
        }
        return instance;
    }
}
