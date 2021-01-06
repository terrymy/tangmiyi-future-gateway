package com.tangmiyi.future.gateway.dao;

import com.tangmiyi.future.gateway.pojo.CityDO;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CityRepository {

    private ConcurrentMap<Long, CityDO> repository = new ConcurrentHashMap<>();

    private static final AtomicLong idGenerator = new AtomicLong(0);

    public Long save(CityDO cityEntity) {
        Long id = idGenerator.incrementAndGet();
        cityEntity.setId(id);
        repository.put(id, cityEntity);
        return id;
    }

    public Collection<CityDO> findAll() {
        return repository.values();
    }


    public CityDO findCityById(Long id) {
        return repository.get(id);
    }

    public Long updateCity(CityDO cityEntity) {
        repository.put(cityEntity.getId(), cityEntity);
        return cityEntity.getId();
    }

    public Long deleteCity(Long id) {
        repository.remove(id);
        return id;
    }
}
