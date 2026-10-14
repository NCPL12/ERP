package com.ncpl.sales.security;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{

	@Query("SELECT u FROM User u WHERE u.username = :username")
	public User findUserByUserName(@Param("username") String username);

	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
	int updatePassword(@Param("userId") Long userId, @Param("password") String password);

}
