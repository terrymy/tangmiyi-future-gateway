package com.tangmiyi.future.gateway.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CityDO {
    /**
     * 城市编号
     */
    private Long id;

    /**
     * 省份编号
     */
    private Long provinceId;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 描述
     */
    private String description;
}
