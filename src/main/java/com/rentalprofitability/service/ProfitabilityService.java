package com.rentalprofitability.service;

import com.rentalprofitability.dto.ProfitabilityRequest;
import com.rentalprofitability.exception.PropertyNotFoundException;
import com.rentalprofitability.model.Platform;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfitabilityService {

    final PropertyRepository repo;
    final BrigthdataService brigthdataService;
    final OpenAIService openAIService;

    @Value("${brightdata.dataset.airbnb.id}")
     private String airbnbDatasetId;

    @Value("${brightdata.dataset.booking.id}")
     private String bookingDatasetId;


    public ProfitabilityService(PropertyRepository repo, BrigthdataService brigthdataService, OpenAIService openAIService){
        this.repo = repo;
        this.brigthdataService = brigthdataService;
        this.openAIService=openAIService;
    }


    private HashMap<Platform, String> getPlatformData(ProfitabilityRequest request){
        Optional<Property> property = repo.findById(request.propertyID());
        String jsonRequest;
        HashMap<Platform,String> jsonRequests = new HashMap<>();

        switch(request.platform()){
            case AIRBNB -> {
                jsonRequest = generateAirbnbRequestJson(property.orElseThrow(()-> new PropertyNotFoundException("Property Not found")));
                return getSingleBrightDataScrappingInfo(Platform.AIRBNB,airbnbDatasetId,jsonRequest);
            }
            case BOOKING -> {
                jsonRequest = generateBookingRequestJson(property.orElseThrow(()-> new PropertyNotFoundException("Property Not found")));
                return getSingleBrightDataScrappingInfo(Platform.BOOKING, bookingDatasetId,jsonRequest);
            }
            case ALL -> {
                // Airbnb
                jsonRequest = generateAirbnbRequestJson(property.orElseThrow(()-> new PropertyNotFoundException("Property Not found")));
                jsonRequests.put(Platform.AIRBNB,jsonRequest);
                // Booking
                jsonRequest = generateBookingRequestJson(property.orElseThrow(()-> new PropertyNotFoundException("Property Not found")));
                jsonRequests.put(Platform.BOOKING,jsonRequest);

                return getMultipleBrightDataScrappingInfo(jsonRequests);
            }
        }
return null;
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

}
