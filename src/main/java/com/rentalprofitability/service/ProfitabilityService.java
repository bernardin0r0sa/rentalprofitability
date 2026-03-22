package com.rentalprofitability.service;

import com.rentalprofitability.dto.ProfitabilityRequest;
import com.rentalprofitability.exception.PropertyNotFoundException;
import com.rentalprofitability.model.Platform;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfitabilityService {

    final PropertyRepository repo;
    final BrightdataService brightdataService;
    final OpenAIService openAIService;

    @Value("${brightdata.dataset.airbnb.id}")
     private String airbnbDatasetId;

    @Value("${brightdata.dataset.booking.id}")
     private String bookingDatasetId;


    public ProfitabilityService(PropertyRepository repo, BrightdataService brightdataService , OpenAIService openAIService){
        this.repo = repo;
        this.brightdataService=brightdataService;
        this.openAIService=openAIService;
    }

    public HashMap<Platform, String> getPlatformData(ProfitabilityRequest request){
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
        String resultJson= brightdataService.scrapeShortRentalPlatforms(dataset,jsonRequest);
        brightDataResults.put(Platform,resultJson);
        return brightDataResults;
         }

    private HashMap<Platform, String> getMultipleBrightDataScrappingInfo(HashMap<Platform,String> jsonRequests){

        String airbnbJsonRequest = jsonRequests.get(Platform.AIRBNB);
        String bookingJsonRequest = jsonRequests.get(Platform.BOOKING);

        HashMap<Platform, String> brightDataResults = new HashMap<>();

        CompletableFuture<String> airbnbFuture = CompletableFuture.supplyAsync(() ->
                brightdataService.scrapeShortRentalPlatforms(airbnbDatasetId,airbnbJsonRequest)
        );

        CompletableFuture<String> bookingFuture = CompletableFuture.supplyAsync(() ->
                brightdataService.scrapeShortRentalPlatforms(bookingDatasetId,bookingJsonRequest)
        );

        // wait for BOTH to finish
        CompletableFuture.allOf(airbnbFuture, bookingFuture).join();

        String airbnbResult = airbnbFuture.join();
        String bookingResult = bookingFuture.join();

        brightDataResults.put(Platform.AIRBNB, airbnbResult);
        brightDataResults.put(Platform.BOOKING, bookingResult);

        return brightDataResults;

    }


    private String generateBookingRequestJson(Property property){
        //TODO
        return null;
    }

    private String generateAirbnbRequestJson(Property property){
        //TODO
return null;
    }
}
