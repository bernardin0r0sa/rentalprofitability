package com.rentalprofitability.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class OccupancyRateConfig {

    private final ObjectMapper mapper;
    private Map<String, Double> occupancyRates;

    public OccupancyRateConfig(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    private void loadOccupancyRates() {
        try {
            occupancyRates = mapper.readValue(
                    getClass().getResourceAsStream("/occupancy-rates.json"),
                    new TypeReference<Map<String, Double>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load occupancy rates");
        }
    }

    public double getOccupancyRate(String city) {
        return occupancyRates.getOrDefault(city, occupancyRates.get("Default"));
    }
}