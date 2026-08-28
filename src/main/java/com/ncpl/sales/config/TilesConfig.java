package com.ncpl.sales.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

@Configuration
public class TilesConfig {
	
	@Bean
	TilesConfigurer tilesConfigurer() {
		TilesConfigurer tilesConfigure = new TilesConfigurer();
		String[] defs = { "WEB-INF/tiles.xml" };
		tilesConfigure.setDefinitions(defs);
		return tilesConfigure;
	}
	
	@Bean
	UrlBasedViewResolver tilesViewResolver() {
		UrlBasedViewResolver tilesViewResolver = new UrlBasedViewResolver();
		tilesViewResolver.setViewClass(TilesView.class);
		return tilesViewResolver;
	}
}
