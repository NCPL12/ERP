package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Make;

@Repository
public interface MakeRepo extends JpaRepository<Make, Integer>{
	@Query( value = "SELECT * FROM  Make where name=:name",nativeQuery = true)
    public List<Make> checkMakeNameExist(@Param("name") String name);
    
    @Query(" from Make where name=?1 and id!=?2")
	List<Make> checkMakeNameExistForEdit(String name, Integer id);
    
    @Query(" from Make where name=?1")
	public Make findMakeByName(String name);
    @Query("SELECT COUNT(u) > 0 FROM Make u WHERE u.name = :name")
	public boolean existsByName(@Param("name") String name);
}
