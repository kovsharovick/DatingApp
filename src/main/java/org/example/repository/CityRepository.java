package org.example.repository;

import org.example.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Integer> {
    List<City> findByCityIgnoreCase(String city);

    @Query("SELECT c FROM City c WHERE LOWER(c.city) LIKE CONCAT(LOWER(:prefix), '%') ORDER BY c.city ASC")
    List<City> searchByPrefix(@Param("prefix") String prefix);
}