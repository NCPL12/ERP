package com.ncpl.sales.security;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model, String error, User user,
            HttpServletRequest request) throws JsonProcessingException {

        if (error != null) {
            model.addAttribute("error", "Invalid user name or password");
        }

        return "login";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /** User Management admin page: list of all users. */
    @GetMapping("/api/users/all")
    @ResponseBody
    public ResponseEntity<?> getAllUsersForManagement() {
        List<User> users = userService.getAllUsers();
        if (users != null && !users.isEmpty()) {
            return ResponseEntity.ok(users);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found");
    }

    @GetMapping("/api/users/current")
    @ResponseBody
    public ResponseEntity<?> getCurrentUserApi() {
        User user = userService.getCurrentUser();
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
    }

    @GetMapping("/api/users/findByUsername")
    @ResponseBody
    public ResponseEntity<?> findByUsername(@RequestParam String username) {
        User user = userService.findByUserName(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with username: " + username);
    }

    @GetMapping("/api/users/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        User user = userService.getUserByUserId(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
    }

    @PostMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> createUser(
            @RequestParam("user_name") String user_name,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            @RequestParam("enabled") Boolean enabled,
            @RequestParam("number") String number,
            @RequestParam("emailId") String emailId,
            @RequestParam("name") String name) {

        try {
            if (userService.userExists(user_name)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Username '" + user_name + "' already exists. Please choose a different username.");
            }

            for (User u : userService.getAllUsers()) {
                if (emailId != null && emailId.equalsIgnoreCase(u.getEmailId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Email '" + emailId + "' already exists. Please use a different email.");
                }
                if (number != null && !number.isEmpty() && number.equals(u.getNumber())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Mobile number '" + number + "' already exists. Please use a different number.");
                }
            }

            User user = new User();
            user.setUsername(user_name);
            user.setPassword(password);
            user.setRole(role);
            user.setEnabled(enabled);
            user.setNumber(number);
            user.setEmailId(emailId);
            user.setName(name);

            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Long id,
            @RequestParam("username") String username,
            @RequestParam("role") String role,
            @RequestParam("number") String number,
            @RequestParam("emailId") String emailId,
            @RequestParam("enabled") Boolean enabled,
            @RequestParam("name") String name,
            HttpServletRequest request,
            HttpServletResponse response) {

        User existingUser = userService.getUserByUserId(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
        }

        if (!existingUser.getUsername().equals(username) && userService.userExists(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username '" + username + "' already exists. Please choose a different username.");
        }

        for (User u : userService.getAllUsers()) {
            if (u.getId().equals(id)) {
                continue;
            }
            if (emailId != null && emailId.equalsIgnoreCase(u.getEmailId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email '" + emailId + "' already exists. Please use a different email.");
            }
            if (number != null && !number.isEmpty() && number.equals(u.getNumber())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Mobile number '" + number + "' already exists. Please use a different number.");
            }
        }

        existingUser.setUsername(username);
        existingUser.setRole(role);
        existingUser.setNumber(number);
        existingUser.setEmailId(emailId);
        existingUser.setEnabled(enabled);
        existingUser.setName(name);

        User updatedUser = userService.updateUser(existingUser);

        // If the same user is being deactivated, force logout the current session
        if (Boolean.FALSE.equals(enabled)) {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                }
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
            }
        }

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/api/users/{id}/password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@PathVariable("id") Long id, @RequestParam("password") String password) {
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password cannot be empty");
        }
        boolean updated = userService.changePassword(id, password);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
        }
        return ResponseEntity.ok("Password updated successfully");
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            User existingUser = userService.getUserByUserId(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
            }
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully with ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting user: " + e.getMessage());
        }
    }
}
