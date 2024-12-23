package com.igodating.geodata.service;

import com.igodating.geodata.model.City;

import java.util.List;
import java.util.function.Function;

public interface CityService {

    <T> T getById(Long id, Function<City, T> mappingFunc);

    <T> List<T> getAll(Function<City, T> mappingFunc);

    Long create(City city);

    Long update(City city);

    Long delete(City city);
}
