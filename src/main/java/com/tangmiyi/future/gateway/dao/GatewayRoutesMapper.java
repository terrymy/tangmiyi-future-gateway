package com.tangmiyi.future.gateway.dao;

import com.tangmiyi.future.gateway.pojo.GatewayRoutesDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface GatewayRoutesMapper extends Mapper<GatewayRoutesDO> {

    int updateByRouteId(@Param("record") GatewayRoutesDO record);

    int deleteByRouteId(@Param("routeId") String routeId);

    List<GatewayRoutesDO> selectByService(@Param("gatewayService") String gatewayService);
}