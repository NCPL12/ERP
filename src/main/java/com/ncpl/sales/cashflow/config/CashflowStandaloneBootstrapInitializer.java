package com.ncpl.sales.cashflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserRepo;

/** Creates the first finance administrator for a new portable standalone database. */
@Component
@Profile("cashflow-standalone")
public class CashflowStandaloneBootstrapInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${cashflow.bootstrap-admin.username:}")
    private String username;

    @Value("${cashflow.bootstrap-admin.password:}")
    private String password;

    public CashflowStandaloneBootstrapInitializer(UserRepo userRepo, BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String requestedUsername = username == null ? "" : username.trim();
        String requestedPassword = password == null ? "" : password;
        if (requestedUsername.isEmpty() || requestedPassword.isEmpty()) {
            return;
        }
        if (userRepo.findUserByUserName(requestedUsername) != null) {
            return;
        }

        User user = new User();
        user.setUsername(requestedUsername);
        user.setPassword(passwordEncoder.encode(requestedPassword));
        user.setRole("SUPER ADMIN");
        user.setEnabled(true);
        user.setName(requestedUsername);
        userRepo.save(user);
    }
}
