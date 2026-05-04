package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.City;
import org.example.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public List<String> autocompleteCity(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        return cityRepository.searchByPrefix(prefix)
                .stream()
                .map(City::getCity)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    public Optional<City> findCityByName(String cityName) {
        List<City> cities = cityRepository.findByCityIgnoreCase(cityName);
        return cities.stream().findFirst();
    }
}