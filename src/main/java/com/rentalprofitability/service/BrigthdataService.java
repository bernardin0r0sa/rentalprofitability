package com.rentalprofitability.service;

import com.rentalprofitability.exception.ExternalApiException;
import com.rentalprofitability.model.Platform;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.util.CountryCodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class BrigthdataService {
    @Value("${brightdata.api.key}")
    private String apiKey;

    @Value("${brightdata.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    public String scrapeShortRentalPlatforms(Platform platform,String datasetId, String jsonRequest) {
        try {

            String fullUrl = buildUrl(platform,datasetId);

            log.info("[BRIGHTDATA] Scraping Airbnb");

            String response = restClient.post()
                    .uri(fullUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonRequest)
                    .retrieve()
                    .body(String.class);

            log.info("[BRIGHTDATA] Scrape successful");
            return response;

        } catch (Exception e) {
            log.error("[BRIGHTDATA] Failed to scrape: {}", e.getMessage());
            throw new ExternalApiException("Failed to call BrightData API: " + e.getMessage());
        }


    }

    private String buildUrl(Platform platform, String dataset) {
        return switch (platform) {
            case AIRBNB -> apiUrl + "?dataset_id=" + dataset +
                    "&notify=false&include_errors=true" +
                    "&type=discover_new&discover_by=location&limit_per_input=30";
            case BOOKING -> apiUrl + "?dataset_id=" + dataset +
                    "&notify=false&include_errors=true";
            default -> throw new ExternalApiException("Unsupported platform: " + platform);
        };
    }

}
