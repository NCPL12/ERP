package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.State;

@Repository
public interface StateRepo extends JpaRepository<State, Long> {

}
