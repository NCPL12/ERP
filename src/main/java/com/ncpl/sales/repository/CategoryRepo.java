package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.ncpl.sales.model.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category,Integer>{
	@Query( value = "SELECT * FROM  Category where name=:name",nativeQuery = true)
	public List<Category> checkCategoryNameExist(@Param("name") String name);

	@Query(" from Category where name=?1 and id!=?2")
	List<Category> checkCategoryNameExistForEdit(String name, Integer id);
}
