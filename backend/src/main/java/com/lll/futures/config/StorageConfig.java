package com.lll.futures.config;

import com.lll.futures.service.StorageService;
import com.lll.futures.service.impl.LocalStorageService;
import com.lll.futures.service.impl.R2StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Storage configuration for environment-based storage selection
 */
@Configuration
public class StorageConfig {
    
    @Value("${storage.type:local}")
    private String storageType;
    
    @Bean
    @Primary
    public StorageService storageService(LocalStorageService localStorageService, 
                                       R2StorageService r2StorageService) {
        if ("r2".equalsIgnoreCase(storageType)) {
            return r2StorageService;
        }
        return localStorageService;
    }
}
