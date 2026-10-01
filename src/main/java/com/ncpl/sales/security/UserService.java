package com.ncpl.sales.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService{

	@Autowired
	private UserRepo userRepo;
	@Autowired
	EncryptedPasswordUtils encryptPasswd;
	@Autowired
	private com.ncpl.sales.repository.RolePermissionRepo rolePermissionRepo;

	//validating user using database
		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			System.out.println("Loading User Obj by UserName: " + username);
			User user = userRepo.findUserByUserName(username);

			if (user == null) {
				throw new UsernameNotFoundException("User not found: " + username);
			}

			Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
			grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole()));   // KEEP THIS — every existing hasAnyAuthority(role name) check still works

			for (com.ncpl.sales.model.RolePermission p : rolePermissionRepo.findByRoleName(user.getRole())) {
				if (p.isCanView())   grantedAuthorities.add(new SimpleGrantedAuthority(p.getModule() + "_VIEW"));
				if (p.isCanEdit())   grantedAuthorities.add(new SimpleGrantedAuthority(p.getModule() + "_EDIT"));
				if (p.isCanDelete()) grantedAuthorities.add(new SimpleGrantedAuthority(p.getModule() + "_DELETE"));
			}

			return new org.springframework.security.core.userdetails.User(
					user.getUsername(),
					user.getPassword(),
					user.isEnabled(),   // enabled
					true,                // accountNonExpired
					true,                // credentialsNonExpired
					true,                // accountNonLocked
					grantedAuthorities);
		}

		/** One user by id, used by the User Management admin page. */
		public User getUserByUserId(Long userId) {
			if (userId == null || userId <= 0) {
				return null;
			}
			return userRepo.findById(userId).orElse(null);
		}

		/** Save a new user (with encrypted password). */
		public User saveUser(User user) {
			if (user.getPassword() != null && !user.getPassword().isEmpty()) {
				user.setPassword(encryptPasswd.encrytePassword(user.getPassword()));
			}
			return userRepo.save(user);
		}

		public User updateUser(User user) {
			return userRepo.save(user);
		}

		public void deleteUser(Long userId) {
			if (userId != null && userId > 0) {
				userRepo.deleteById(userId);
			}
		}

		public boolean userExists(String username) {
			return userRepo.findUserByUserName(username) != null;
		}

		public boolean changePassword(Long userId, String password) {
			User user = userRepo.findById(userId).orElse(null);
			if (user == null) {
				return false;
			}
			String encryptedPassword = encryptPasswd.encrytePassword(password);
			int updatedRows = userRepo.updatePassword(userId, encryptedPassword);
			return updatedRows > 0;
		}

	public User save() {
		/*User user = new User();
		user.setUsername("anvesh");
		user.setPassword(encryptPasswd.encrytePassword("anvesh@123#"));
		user.setRole("NORMAL USER");
		user.setEnabled(true);
		userRepo.save(user);
		
		User user1 = new User();
		user1.setUsername("freeda");
		user1.setPassword(encryptPasswd.encrytePassword("123#freeda"));
		user1.setRole("NORMAL USER");
		user1.setEnabled(true);
		userRepo.save(user1);
		
		User user2 = new User();
		user2.setUsername("surendra");
		user2.setPassword(encryptPasswd.encrytePassword("admin_surendra"));
		user2.setRole("NORMAL USER");
		user2.setEnabled(true);
		userRepo.save(user2);*/
		
		// User user = new User();
		// user.setUsername("HI");
		// user.setPassword(encryptPasswd.encrytePassword("hi@123"));
		// user.setRole("ITEMMASTER");
		// user.setEnabled(true);
		// userRepo.save(user);
	
		
		
		return null;
	}
	
	public User findByUserName(String userName) {
		return userRepo.findUserByUserName(userName);
	}
	
public List<User> getAllUsers() {
    return userRepo.findAll();
}
	
	public User getCurrentUser() {
		User user = null;
		try {
			String userName = SecurityContextHolder.getContext().getAuthentication().getName();
			user = findByUserName(userName);
		} catch (Exception e) {
			System.out.println("Error getting current user: " + e.getMessage());
		}
		return user;
	}
}
