package com.rentalprofitability.service;

import com.rentalprofitability.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.temperature}")
    private double temperature;

    @Value("${openai.api.max-tokens}")
    private int maxTokens;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public String callOpenAI(String prompt) {
        String requestBody = """
            {
                "model": "%s",
                "temperature": %s,
                "max_tokens": %d,
                "messages": [{"role": "user", "content": "%s"}]
            }
            """.formatted(model, temperature, maxTokens, prompt);

        try {
            String response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractContent(response);

        } catch (Exception e) {
            log.error("[OPENAI] Failed to call API: {}", e.getMessage());
            throw new ExternalApiException("Failed to call OpenAI API: " + e.getMessage());
        }
    }

    private String extractContent(String response) {
        try {
            JsonNode root = mapper.readTree(response);
            return root.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();
        } catch (Exception e) {
            log.error("[OPENAI] Failed to parse response: {}", e.getMessage());
            throw new ExternalApiException("Failed to parse OpenAI response");
        }
    }
}
