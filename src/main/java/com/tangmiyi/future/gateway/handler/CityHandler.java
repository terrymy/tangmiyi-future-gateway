package com.tangmiyi.future.gateway.handler;

import com.tangmiyi.future.gateway.dao.CityRepository;
import com.tangmiyi.future.gateway.pojo.CityDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CityHandler {

    private final CityRepository cityRepository;

    @Autowired
    public CityHandler(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public Mono<Long> save(CityDO city) {
        return Mono.create(cityMonoSink -> cityMonoSink.success(cityRepository.save(city)));
    }

    /**
     * Mono：实现发布者，并返回 0 或 1 个元素，即单对象
     */
    public Mono<CityDO> findCityById(Long id) {
        return Mono.justOrEmpty(cityRepository.findCityById(id));
    }

    /**
     * Flux：实现发布者，并返回 N 个元素，即 List 列表对象
     * @return
     */
    public Flux<CityDO> findAllCity() {
        return Flux.fromIterable(cityRepository.findAll());
    }

    public Mono<Long> modifyCity(CityDO city) {
        return Mono.create(cityMonoSink -> cityMonoSink.success(cityRepository.updateCity(city)));
    }

    public Mono<Long> deleteCity(Long id) {
        return Mono.create(cityMonoSink -> cityMonoSink.success(cityRepository.deleteCity(id)));
    }
}
