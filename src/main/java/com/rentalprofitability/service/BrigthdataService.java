package com.rentalprofitability.service;

import com.rentalprofitability.exception.ExternalApiException;
import com.rentalprofitability.model.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class BrigthdataService {
    @Value("${brightdata.api.key}")
    private String apiKey;

    @Value("${brightdata.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public String scrapeShortRentalPlatforms(Platform platform, String datasetId, String jsonRequest) {
        try {
            String fullUrl = buildUrl(platform, datasetId);
            log.info("[BRIGHTDATA] Triggering scrape for platform: {}", platform);

            String response = restClient.post()
                    .uri(fullUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonRequest)
                    .retrieve()
                    .body(String.class);

            String snapshotId = extractSnapshotId(response);
            if (snapshotId!=null) {
                log.info("[BRIGHTDATA] Snapshot ID received: {}", snapshotId);
                return pollUntilReady(snapshotId);
            }

            log.info("[BRIGHTDATA] Scrape successful for platform: {}", platform);
            return response;

        } catch (ExternalApiException e) {
            throw e;
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

    private String extractSnapshotId(String response) {
        if (response.trim().startsWith("{\"url\"") ||
                response.trim().startsWith("[{\"url\"")) {
            return null;
        }

        try {
            String firstObject = response.trim().split("\n")[0];
            JsonNode root = mapper.readTree(firstObject);
            if (root.has("snapshot_id")) {
                return root.get("snapshot_id").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String pollUntilReady(String snapshotId) {
        String snapshotUrl = "https://api.brightdata.com/datasets/v3/snapshot/"
                + snapshotId + "?format=json";

        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(30000);

                String result = restClient.get()
                        .uri(snapshotUrl)
                        .header("Authorization", "Bearer " + apiKey)
                        .retrieve()
                        .body(String.class);

                if (!result.contains("\"status\":\"running\"")) {
                    log.info("[BRIGHTDATA] Data ready after {} attempts", i + 1);
                    return result;
                }

                log.info("[BRIGHTDATA] Still processing, attempt {}/10", i + 1);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalApiException("Polling interrupted");
            }
        }
        throw new ExternalApiException("BrightData timeout - data not ready after 50 seconds");
    }



}
