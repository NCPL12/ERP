/*
 * @Author
 * @Copyright
 * @Date
 */
package com.ncpl.sales;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SalesApp extends SpringBootServletInitializer{

	public static void main(String[] args) {
	//	System.setProperty("spring.devtools.restart.enabled", "false"); //On change of any file avoid restart
		SpringApplication.run(SalesApp.class, args);
	}
	
	/**
	 * For running as a web application
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		 return builder.sources(SalesApp.class);
	}
}

