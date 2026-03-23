package com.rentalprofitability.service;

import com.rentalprofitability.dto.ProfitabilityCompareResponse;
import com.rentalprofitability.dto.ProfitabilityRequest;
import com.rentalprofitability.dto.ProfitabilityResponse;
import com.rentalprofitability.model.Platform;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.model.RentalType;
import com.rentalprofitability.util.OccupancyRateConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfitabilityService {

    final PropertyService propertyService;
    final BrigthdataService brigthdataService;
    final OpenAIService openAIService;
    final OccupancyRateConfig occupancyRateConfig;

    @Value("${brightdata.dataset.airbnb.id}")
    private String airbnbDatasetId;

    @Value("${brightdata.dataset.booking.id}")
    private String bookingDatasetId;

    private final ObjectMapper mapper = new ObjectMapper();


    public ProfitabilityService(PropertyService propertyService, BrigthdataService brigthdataService, OpenAIService openAIService, OccupancyRateConfig occupancyRateConfig){
        this.propertyService = propertyService;
        this.brigthdataService = brigthdataService;
        this.openAIService=openAIService;
        this.occupancyRateConfig = occupancyRateConfig;
    }

    public ProfitabilityResponse getProfitability(ProfitabilityRequest request) {
        Property property = propertyService.getProperty(request.propertyID());
        HashMap<Platform, String> marketData = getPlatformData(property, request.platform());

        double averageNightlyRate = switch (request.rentaltype()) {
            case SHORT -> calculateAverage(marketData);
            case LONG -> calculateAverage(marketData);
            case COMPARE -> throw new RuntimeException("Use /compare endpoint");
        };

        return buildProfitabilityResponse(property, request.rentaltype(),
                averageNightlyRate, request.propertyManagementFee());
    }

    public ProfitabilityCompareResponse getCompareProfitability(ProfitabilityRequest request) {
        Property property = propertyService.getProperty(request.propertyID());
        HashMap<Platform, String> marketData = getPlatformData(property, request.platform());

        ProfitabilityResponse shortResponse = buildProfitabilityResponse(
                property, RentalType.SHORT, calculateAverage(marketData), request.propertyManagementFee());

        ProfitabilityResponse longResponse = buildProfitabilityResponse(
                property, RentalType.LONG, calculateAverage(marketData), request.propertyManagementFee());

        return new ProfitabilityCompareResponse(shortResponse, longResponse);
    }

    private HashMap<Platform, String> getPlatformData(Property property, Platform platform){

        String jsonRequest;
        HashMap<Platform,String> jsonRequests = new HashMap<>();
        HashMap<Platform, String> marketData = new HashMap<>();;

        switch(platform){
            case AIRBNB -> {
                jsonRequest = generateAirbnbRequestJson(property);
                marketData =  getSingleBrightDataScrappingInfo(Platform.AIRBNB,airbnbDatasetId,jsonRequest);
            }
            case BOOKING -> {
                jsonRequest = generateBookingRequestJson(property);
                marketData =   getSingleBrightDataScrappingInfo(Platform.BOOKING, bookingDatasetId,jsonRequest);
            }
            case ALL -> {
                // Airbnb
                jsonRequest = generateAirbnbRequestJson(property);
                jsonRequests.put(Platform.AIRBNB,jsonRequest);
                // Booking
                jsonRequest = generateBookingRequestJson(property);
                jsonRequests.put(Platform.BOOKING,jsonRequest);

                marketData =   getMultipleBrightDataScrappingInfo(jsonRequests);
            }
        }
        return marketData;
    }


    private HashMap<Platform, String> getSingleBrightDataScrappingInfo(Platform Platform, String dataset, String jsonRequest){
        HashMap<Platform, String> brightDataResults = new HashMap<>();
        String resultJson= brigthdataService.scrapeShortRentalPlatforms(dataset,jsonRequest);
        brightDataResults.put(Platform,resultJson);
        return brightDataResults;
    }

    private HashMap<Platform, String> getMultipleBrightDataScrappingInfo(HashMap<Platform,String> jsonRequests){

        String airbnbJsonRequest = jsonRequests.get(Platform.AIRBNB);
        String bookingJsonRequest = jsonRequests.get(Platform.BOOKING);

        HashMap<Platform, String> brightDataResults = new HashMap<>();

        CompletableFuture<String> airbnbFuture = CompletableFuture.supplyAsync(() ->
                brigthdataService.scrapeShortRentalPlatforms(airbnbDatasetId,airbnbJsonRequest)
        );

        CompletableFuture<String> bookingFuture = CompletableFuture.supplyAsync(() ->
                brigthdataService.scrapeShortRentalPlatforms(bookingDatasetId,bookingJsonRequest)
        );

        // wait for BOTH to finish
        CompletableFuture.allOf(airbnbFuture, bookingFuture).join();

        String airbnbResult = airbnbFuture.join();
        String bookingResult = bookingFuture.join();

        brightDataResults.put(Platform.AIRBNB, airbnbResult);
        brightDataResults.put(Platform.BOOKING, bookingResult);

        return brightDataResults;

    }


    private String generateBookingRequestJson(Property property) {
        int guests = calculateGuests(property);
        int rooms = (int) property.getBedrooms();
        String checkIn = LocalDate.now().atStartOfDay().toString() + "Z";
        String checkOut = LocalDate.now().plusDays(1).atStartOfDay().toString() + "Z";

        return """
            {
                "input": [{
                    "url": "https://www.booking.com",
                    "location": "%s, %s",
                    "check_in": "%s",
                    "check_out": "%s",
                    "adults": %d,
                    "rooms": %d,
                    "country": "%s",
                    "property_type": "Entire homes & apartments",
                    "currency": "EUR"
                }]
            }
            """.formatted(property.getCity(), property.getCountry(),
                checkIn, checkOut,
                guests, rooms,
                property.getCountry());
    }

    private String generateAirbnbRequestJson(Property property) {
        int guests = calculateGuests(property);
        String checkIn = LocalDate.now().atStartOfDay().toString() + "Z";
        String checkOut = LocalDate.now().plusDays(1).atStartOfDay().toString() + "Z";

        return """
            {
                "input": [{
                    "location": "%s, %s",
                    "check_in": "%s",
                    "check_out": "%s",
                    "num_of_adults": %d,
                    "num_of_children": 0,
                    "num_of_infants": 0,
                    "num_of_pets": 0,
                    "currency": "EUR",
                    "country": "%s"
                }]
            }
            """.formatted(property.getCity(), property.getCountry(),
                checkIn, checkOut,
                guests, property.getCountry());
    }

    private int calculateGuests(Property property) {
        return (int) property.getBedrooms() * 2;
    }
    private double parseAirbnbAveragePrice(String jsonResponse) {
        try {
            JsonNode root = mapper.readTree(jsonResponse);
            List<Double> prices = new ArrayList<>();

            for (JsonNode listing : root) {
                JsonNode pricingDetails = listing.get("pricing_details");
                if (pricingDetails != null && pricingDetails.has("price_per_night")) {
                    prices.add(pricingDetails.get("price_per_night").asDouble());
                }
            }

            return calculateAverageFromSingleMarket(prices);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Airbnb response");
        }
    }

    private double parseBookingAveragePrice(String jsonResponse) {
        try {
            JsonNode root = mapper.readTree(jsonResponse);
            List<Double> prices = new ArrayList<>();

            for (JsonNode listing : root) {
                if (listing.has("original_price")) {
                    prices.add(listing.get("original_price").asDouble());
                }
            }

            return calculateAverageFromSingleMarket(prices);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Booking response");

        }
    }

    private double calculateAverageFromSingleMarket(List<Double> prices) {
        if (prices.isEmpty()) {
            throw new RuntimeException("Failed to parse Airbnb response");
        }
        return prices.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElseThrow(() -> new RuntimeException("Failed to calculate average price"));
    }

    private double calculateAverage(HashMap<Platform, String> marketData) {
        double total = 0;

        for (Map.Entry<Platform, String> entry : marketData.entrySet()) {
            if (entry.getKey().equals(Platform.AIRBNB)) {
                total += parseAirbnbAveragePrice(entry.getValue());
            } else {
                total += parseBookingAveragePrice(entry.getValue());
            }
        }

        return total / marketData.size();
    }

    private ProfitabilityResponse buildProfitabilityResponse(
            Property property,
            RentalType rentalType,
            double averageNightlyRate,
            double propertyManagementFee) {

        double occupancyRate = occupancyRateConfig.getOccupancyRate(property.getCity());
        double estimatedMonthlyRevenue = averageNightlyRate * 30 * occupancyRate;
        double estimatedYearlyRevenue = estimatedMonthlyRevenue * 12;
        double managementFeeAmount = estimatedMonthlyRevenue * (propertyManagementFee / 100);
        double netMonthlyProfit = estimatedMonthlyRevenue - (property.getMortgage() + property.getUtilities() + managementFeeAmount);
        double ROI = (netMonthlyProfit * 12 / property.getCashInvested()) * 100;
        String result = String.format(
                "Based on a cash investment of €%.0f, with an estimated monthly revenue of €%.0f, your annual ROI is %.1f%%.",
                property.getCashInvested(), estimatedMonthlyRevenue, ROI
        );

        return new ProfitabilityResponse(
                property.getId(),
                rentalType,
                estimatedMonthlyRevenue,
                estimatedYearlyRevenue,
                ROI,
                result
        );
    }


}
