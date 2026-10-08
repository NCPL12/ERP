package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ncpl.sales.model.AppConfig;

public interface AppConfigRepo extends JpaRepository<AppConfig, Integer> {
	AppConfig findByConfigKey(String configKey);
}
