package com.ncpl.sales.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Units;
@Repository
public interface UnitsRepo extends JpaRepository<Units,Long> {

	@Query(" from Units where name=?1 ")
	Units findByName(String name);
	@Query(" from Units where name=?1 ")
	boolean existsByName(String unit);

}
