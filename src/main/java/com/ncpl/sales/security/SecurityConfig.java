package com.ncpl.sales.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

		//User details
		@Autowired
		UserService userDetailsService;
		//Custom login handler
		@Autowired
		LoginSuccessHandler customLoginSuccessHandler;
		
		//Encrypt password
		@Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
	        return bCryptPasswordEncoder;
	    }
		
		//Check user in DB
		@Autowired
		protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {	
			auth.userDetailsService(userDetailsService);
		}
		
		//URL security
		@Override
		 protected void configure(HttpSecurity http) throws Exception {
		 http.csrf().disable();	
		 http
		 .authorizeRequests()
		 .antMatchers("/css/**", "/js/**", "/images/**","/resources/**", "/api/**", "/login/**").permitAll()
		 .antMatchers("/sales_report/**").hasAnyAuthority("ADMIN","PURCHASE","SUPER ADMIN","SALES","PURCHASE STORE")
		 .antMatchers("/itemMaster/**").hasAnyAuthority("ITEMMASTER","ADMIN","NORMAL USER","PURCHASE","STORE","SUPER ADMIN","STORE USER","PURCHASE STORE")
		 .antMatchers("/**").hasAnyAuthority("ADMIN","NORMAL USER","ITEMMASTER","PURCHASE","STORE","SUPER ADMIN","PURCHASE STORE","STORE USER","SALES")
		 .anyRequest().authenticated()
		 .and()
		 .formLogin().successHandler(customLoginSuccessHandler)
		 .loginPage("/login")
		 .permitAll()
		 .and()
		 .logout()
		 .permitAll()
		 .and()
		 .exceptionHandling().accessDeniedPage("/login");
		 }
		
		
}
