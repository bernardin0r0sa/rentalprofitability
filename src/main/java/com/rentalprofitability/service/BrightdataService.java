package com.rentalprofitability.service;

import com.rentalprofitability.exception.ExternalApiException;
import com.rentalprofitability.model.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class BrightdataService {
    @Value("${brightdata.api.key}")
    private String apiKey;

    @Value("${brightdata.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public String scrapeShortRentalPlatforms(String datasetId, String jsonRequest) {
        try {
            int guests = calculateGuests(property);

            String requestBody = """
                {
                    "input": [{
                        "location": "%s, %s",
                        "check_in": "2025-08-01T00:00:00.000Z",
                        "check_out": "2025-08-05T00:00:00.000Z",
                        "num_of_adults": %d,
                        "num_of_children": 0,
                        "num_of_infants": 0,
                        "num_of_pets": 0,
                        "currency": "EUR",
                        "country": "%s"
                    }]
                }
                """.formatted(property.getCity(), property.getCountry(), guests, property.getCountry());

            String fullUrl = apiUrl + "?dataset_id=" + datasetId +
                    "&notify=false&include_errors=true" +
                    "&type=discover_new&discover_by=location&limit_per_input=100";

            log.info("[BRIGHTDATA] Scraping Airbnb for {} bedrooms ({} guests) in {}, {}",
                    (int) property.getBedrooms(), guests, property.getCity(), property.getCountry());

            String response = restClient.post()
                    .uri(fullUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("[BRIGHTDATA] Scrape successful");
            return response;

        } catch (Exception e) {
            log.error("[BRIGHTDATA] Failed to scrape: {}", e.getMessage());
            throw new ExternalApiException("Failed to call BrightData API: " + e.getMessage());
        }
    }

}
