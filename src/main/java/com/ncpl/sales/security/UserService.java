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
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EncryptedPasswordUtils encryptPasswd;

    /**
     * Validating user using database
     * Used by Spring Security during authentication
     */
    // @Override
    // public UserDetails loadUserByUsername(String username)
    //         throws UsernameNotFoundException {

    //     System.out.println("Loading User Obj by UserName");

    //     User user = userRepo.findUserByUserName(username);

    //     if (user != null) {

    //         Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

    //         grantedAuthorities.add(
    //             new SimpleGrantedAuthority(user.getRole())
    //         );

    //         return new org.springframework.security.core.userdetails.User(
    //             user.getUsername(),
    //             user.getPassword(),
    //             grantedAuthorities
    //         );

    //     } else {
    //         throw new BadCredentialsException(
    //             "Invalid Username or Password"
    //         );
    //     }
    // }
    @Override
public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

    System.out.println("Loading User Obj by UserName: " + username);

    User user = userRepo.findUserByUserName(username);

    if (user == null) {
        System.out.println("USER NOT FOUND IN DATABASE: " + username);

        throw new UsernameNotFoundException(
            "User not found: " + username
        );
    }

    System.out.println("USER FOUND: " + user.getUsername());
    System.out.println("ROLE: " + user.getRole());
    System.out.println("ENABLED: " + user.isEnabled());
    System.out.println("PASSWORD EXISTS: " +
        (user.getPassword() != null && !user.getPassword().isEmpty()));

    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

    grantedAuthorities.add(
        new SimpleGrantedAuthority(user.getRole())
    );

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        user.isEnabled(),   // enabled
        true,                // accountNonExpired
        true,                // credentialsNonExpired
        true,                // accountNonLocked
        grantedAuthorities
    );
}

    /**
     * Find user by username
     */
    public User findByUserName(String userName) {
        return userRepo.findUserByUserName(userName);
    }

    /**
     * Get all users from database
     */
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    /**
     * Get one user by user_id
     */
    public User getUserByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userRepo.findById(userId).orElse(null);
    }

    /**
     * Get currently logged-in user from Security Context
     */
    public User getCurrentUser() {

        User user = null;

        try {
            String userName =
                SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            user = findByUserName(userName);
        } catch (Exception e) {
            System.out.println("Error getting current user: " + e.getMessage());
        }

        return user;
    }

    /**
     * Save new user (with encrypted password)
     */
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Make sure method name matches your EncryptedPasswordUtils class
            user.setPassword(encryptPasswd.encrytePassword(user.getPassword()));
        }
        return userRepo.save(user);
    }

    /**
     * Update user
     */
    public User updateUser(User user) {
        return userRepo.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long userId) {
        if (userId != null && userId > 0) {
            userRepo.deleteById(userId);
        }
    }

    /**
     * Check if user exists by username
     */
    public boolean userExists(String username) {
        return userRepo.findUserByUserName(username) != null;
    }

    /**
     * Method to create sample users (commented out - use saveUser instead)
     */
    public User save() {

        /*
        User user = new User();
        user.setUsername("anvesh");
        user.setPassword(
            encryptPasswd.encrytePassword("anvesh@123#")
        );
        user.setRole("NORMAL USER");
        user.setEnabled(true);
        userRepo.save(user);

        User user1 = new User();
        user1.setUsername("freeda");
        user1.setPassword(
            encryptPasswd.encrytePassword("123#freeda")
        );
        user1.setRole("NORMAL USER");
        user1.setEnabled(true);
        userRepo.save(user1);

        User user2 = new User();
        user2.setUsername("surendra");
        user2.setPassword(
            encryptPasswd.encrytePassword("admin_surendra")
        );
        user2.setRole("NORMAL USER");
        user2.setEnabled(true);
        userRepo.save(user2);
        */

        /*
        User user = new User();
        user.setUsername("ItemMaster");
        user.setPassword(
            encryptPasswd.encrytePassword("Master@123")
        );
        user.setRole("ITEMMASTER");
        user.setEnabled(true);
        userRepo.save(user);
        */

        return null;
    }
	public boolean changePassword(Long userId, String password) {

    User user = userRepo.findById(userId).orElse(null);

    if (user == null) {
        return false;
    }

    String encryptedPassword =
            encryptPasswd.encrytePassword(password);

    int updatedRows =
            userRepo.updatePassword(userId, encryptedPassword);

    return updatedRows > 0;
}
}