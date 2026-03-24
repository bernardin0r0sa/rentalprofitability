package com.rentalprofitability.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class CountryCodeConfig {

    private final ObjectMapper mapper;
    private Map<String, String> countryCodes;

    public CountryCodeConfig(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    private void loadCountryCodes() {
        try {
            countryCodes = mapper.readValue(
                    getClass().getResourceAsStream("/country-codes.json"),
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load country codes");
        }
    }

    public String getCountryCode(String country) {
        return countryCodes.getOrDefault(country, "");
    }
}