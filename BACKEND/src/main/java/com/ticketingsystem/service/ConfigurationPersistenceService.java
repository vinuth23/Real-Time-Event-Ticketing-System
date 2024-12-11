package com.ticketingsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.ticketingsystem.model.ConfigurationUpdate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ConfigurationPersistenceService {
    private static final String CONFIG_FILE = "system-config.json";
    private final ObjectMapper objectMapper;

    public ConfigurationPersistenceService() {
        this.objectMapper = new ObjectMapper();
    }

    public void saveConfiguration(ConfigurationUpdate config) {
        try {
            objectMapper.writeValue(new File(CONFIG_FILE), config);
            System.out.println("Configuration saved to file: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    public ConfigurationUpdate loadConfiguration() {
        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                // Return default configuration if file doesn't exist
                return new ConfigurationUpdate(
                        100,    // maxTicketCapacity
                        2,      // releaseInterval
                        2,      // purchaseInterval
                        1,      // ticketsPerRelease
                        100,    // maxTicketCapacity
                        1,      // numVendors
                        1       // numCustomers
                );
            }
            return objectMapper.readValue(new File(CONFIG_FILE), ConfigurationUpdate.class);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}