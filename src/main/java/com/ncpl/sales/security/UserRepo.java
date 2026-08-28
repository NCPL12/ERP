package com.ncpl.sales.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{
	
	@Query("SELECT u FROM User u WHERE u.username = :username")
	public User findUserByUserName(@Param("username") String username);

}
