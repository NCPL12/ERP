package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.ncpl.sales.model.Type;
@Repository
public interface TypeRepo extends JpaRepository<Type, Integer>{
    @Query( value = "SELECT * FROM  Type where name=:name",nativeQuery = true)
    public List<Type> checkTypeNameExist(@Param("name") String name);
    
    @Query(" from Type where name=?1 and id!=?2")
	List<Type> checkTypeNameExistForEdit(String name, Integer id);
}
