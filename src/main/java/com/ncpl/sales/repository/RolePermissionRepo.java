package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.RolePermission;

@Repository
public interface RolePermissionRepo extends JpaRepository<RolePermission, Long> {

	// join through role.name — users.role is unchanged
	@Query("select p from RolePermission p where p.role.name = :name")
	List<RolePermission> findByRoleName(@Param("name") String name);

	List<RolePermission> findByRole_RoleId(Long roleId);

	void deleteByRole_RoleId(Long roleId);
}
