package com.ncpl.sales.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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
	
	//validating user using database 
		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		    System.out.println("Loading User Obj by UserName");
			User user = userRepo.findUserByUserName(username);

			if (user != null) {
				Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
				//for (Role role : user.getRoles()){
					grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole()));
				//}
				return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),grantedAuthorities);
			} else {
				throw new BadCredentialsException("Invalid Username or Password");
			}
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
		
		/*User user = new User();
		user.setUsername("ItemMaster");
		user.setPassword(encryptPasswd.encrytePassword("Master@123"));
		user.setRole("ITEMMASTER");
		user.setEnabled(true);
		userRepo.save(user);
		*/
		
		
		return null;
	}
	
	public User findByUserName(String userName) {
		return userRepo.findUserByUserName(userName);
	}
	
	public List<User> getAllUsers(){
		List<User> userList=userRepo.findAll();
		return userList;
	}
	
	public User getCurrentUser() {
		User user = null;
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		user = findByUserName(userName);
		return user;
	}
}
