package com.igodating.geodata.service.impl;

import com.igodating.commons.exception.ValidationException;
import com.igodating.geodata.model.City;
import com.igodating.geodata.repository.CityRepository;
import com.igodating.geodata.service.CityService;
import com.igodating.geodata.service.validation.CityValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityValidationService cityValidationService;

    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public <T> T getById(Long id, Function<City, T> mappingFunc) {
        return cityRepository.findById(id).map(mappingFunc).orElseThrow(() -> new ValidationException("Entity not found by id"));
    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> getAll(Function<City, T> mappingFunc) {
        return cityRepository.findAll().stream().map(mappingFunc).toList();
    }

    @Override
    @Transactional
    public <T> Long create(T cityCreateRequest, Function<T, City> mappingFunc) {
        City city = Optional.of(cityCreateRequest).map(mappingFunc).orElse(null);

        cityValidationService.validateOnCreate(city);

        cityRepository.save(city);

        return city.getId();
    }

    @Override
    @Transactional
    public <T> Long update(T cityUpdateRequest, Function<T, City> mappingFunc) {
        City city = Optional.of(cityUpdateRequest).map(mappingFunc).orElse(null);

        cityValidationService.validateOnUpdate(city);

        cityRepository.save(city);

        return city.getId();
    }

    @Override
    @Transactional
    public <T> Long delete(T cityDeleteRequest, Function<T, City> mappingFunc) {
        City city = Optional.of(cityDeleteRequest).map(mappingFunc).orElse(null);

        cityValidationService.validateOnDelete(city);

        Long id = city.getId();

        cityRepository.logicalDelete(id);

        return id;
    }
}
