package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Returnable;

@Repository
public interface ReturnableRepo extends JpaRepository<Returnable, Integer> {

}
