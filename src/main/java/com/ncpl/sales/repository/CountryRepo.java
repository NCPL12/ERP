package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ncpl.sales.model.Country;

@Repository
public interface CountryRepo extends JpaRepository<Country, Integer> {
    
}