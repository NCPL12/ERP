package com.ncpl.sales.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get user details by User ID
     * URL: GET http://localhost:8080/api/users/3
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        User user = userService.getUserByUserId(userId);
        
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found with ID: " + userId);
        }
    }

    /**
     * Get all users
     * URL: GET http://localhost:8080/api/users/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        
        if (users != null && !users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No users found");
        }
    }

    /**
     * Get currently logged-in user details
     * URL: GET http://localhost:8080/api/users/current
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        User user = userService.getCurrentUser();
        
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("No user logged in");
        }
    }

    /**
     * Get user by username
     * URL: GET http://localhost:8080/api/users/findByUsername?username=surendra
     */
    @GetMapping("/findByUsername")
    public ResponseEntity<?> findByUsername(@RequestParam String username) {
        User user = userService.findByUserName(username);
        
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found with username: " + username);
        }
    }
	@PostMapping
public ResponseEntity<?> createUser(
        // @RequestParam("id") Long id,
        @RequestParam("user_name") String user_name,
        @RequestParam("password") String password,
        @RequestParam("role") String role,
        @RequestParam("enabled") Boolean enabled,
        @RequestParam("number") String number,
        @RequestParam("emailId") String emailId,
        @RequestParam("name") String name) {

    try {

        // Prevent duplicate usernames
        if (userService.userExists(user_name)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Username '" + user_name + "' already exists. Please choose a different username.");
        }

        // Prevent duplicate email and mobile number
        for (User u : userService.getAllUsers()) {

            if (emailId != null && emailId.equalsIgnoreCase(u.getEmailId())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Email '" + emailId + "' already exists. Please use a different email.");
            }

            if (number != null && !number.isEmpty() && number.equals(u.getNumber())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Mobile number '" + number + "' already exists. Please use a different number.");
            }
        }

        User user = new User();

        // Remove this line because User has no setUser_id()
        // user.setUser_id(id);

        user.setUsername(user_name);
        user.setPassword(password);
        user.setRole(role);
        user.setEnabled(enabled);
        user.setNumber(number);
        user.setEmailId(emailId);
        user.setName(name);

        User savedUser = userService.saveUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);

    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Error creating user: " + e.getMessage());
    }
}
@PutMapping("/{id}")
public ResponseEntity<?> updateUser(
        @PathVariable("id") Long id,
        @RequestParam("username") String username,
        @RequestParam("role") String role,
        @RequestParam("number") String number,
        @RequestParam("emailId") String emailId,
        @RequestParam("enabled") Boolean enabled,
        @RequestParam("name") String name) {

    User existingUser = userService.getUserByUserId(id);

    if (existingUser == null) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User not found with ID: " + id);
    }

    // Prevent duplicate usernames (allow keeping the same username as before)
    if (!existingUser.getUsername().equals(username)
            && userService.userExists(username)) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Username '" + username + "' already exists. Please choose a different username.");
    }

    // Prevent duplicate email and mobile number (exclude this same user's own record)
    for (User u : userService.getAllUsers()) {

        if (u.getId().equals(id)) {
            continue;
        }

        if (emailId != null && emailId.equalsIgnoreCase(u.getEmailId())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email '" + emailId + "' already exists. Please use a different email.");
        }

        if (number != null && !number.isEmpty() && number.equals(u.getNumber())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Mobile number '" + number + "' already exists. Please use a different number.");
        }
    }

    existingUser.setUsername(username);
    existingUser.setRole(role);
    existingUser.setNumber(number);
    existingUser.setEmailId(emailId);
    existingUser.setEnabled(enabled);
    existingUser.setName(name);

    // ID, password and enabled stay unchanged
    User updatedUser = userService.updateUser(existingUser);

    return ResponseEntity.ok(updatedUser);
}

@PutMapping("/{id}/password")
public ResponseEntity<?> changePassword(
        @PathVariable("id") Long id,
        @RequestParam("password") String password) {

    if (password == null || password.trim().isEmpty()) {
        return ResponseEntity
                .badRequest()
                .body("Password cannot be empty");
    }

    boolean updated = userService.changePassword(id, password);

    if (!updated) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User not found with ID: " + id);
    }

    return ResponseEntity.ok("Password updated successfully");
}
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteUser(
        @PathVariable("id") Long id) {

    try {
        User existingUser = userService.getUserByUserId(id);

        if (existingUser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with ID: " + id);
        }

        userService.deleteUser(id);

        return ResponseEntity
                .ok("User deleted successfully with ID: " + id);

    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Error deleting user: " + e.getMessage());
    }
}
}