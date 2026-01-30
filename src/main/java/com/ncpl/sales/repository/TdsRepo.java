package com.ncpl.sales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Tds;

@Repository
public interface TdsRepo extends JpaRepository<Tds, Integer>{
	
	@Query(" from Tds where soNumber=?1 ")
	Optional<Tds> getTdsBySoNumber(String soNumber);
	
	@Query(" from Tds where soNumber=?1 ")
	List<Tds> getTdsListBySoNumber(String soNumber);

}
