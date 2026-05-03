package org.example.repository;

import org.example.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Integer> {
    List<City> findByCityIgnoreCase(String city);
}