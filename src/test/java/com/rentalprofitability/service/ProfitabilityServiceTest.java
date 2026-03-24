package com.rentalprofitability.service;


import com.rentalprofitability.dto.CreatePropertyRequest;
import com.rentalprofitability.dto.ProfitabilityCompareResponse;
import com.rentalprofitability.dto.ProfitabilityRequest;
import com.rentalprofitability.dto.ProfitabilityResponse;
import com.rentalprofitability.model.Platform;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.model.RentalType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
public class ProfitabilityServiceTest {

    @Mock
    private com.rentalprofitability.service.BrigthdataService brigthdataService;

    @Mock
    private com.rentalprofitability.service.OpenAIService openAIService;

    @Mock
    private com.rentalprofitability.service.PropertyService propertyService;

    @Mock
    private com.rentalprofitability.util.OccupancyRateConfig occupancyRateConfig;

    @Mock
    private com.rentalprofitability.util.CountryCodeConfig countryCodeConfig;


    @InjectMocks
    private com.rentalprofitability.service.ProfitabilityService profitabilityService;

    @Test
    void generateAirbnbShortProfitability_shouldGenerateAndReturnProfitabilityResponse() {

        ProfitabilityRequest profitabilityRequest = new ProfitabilityRequest(1L, RentalType.SHORT, Platform.AIRBNB,16,"Euro");

        String resultJsonAirbnb = """
                [
                  {
                    "name": "Rental unit in Funchal · ★4.72 · 2 bedrooms · 4 beds · 1 bath",
                    "price": 95,
                    "pricing_details": {
                      "num_of_nights": 1,
                      "initial_price_per_night": 95,
                      "price_per_night": 95,
                      "price_without_fees": 95
                    },
                    "location": "Funchal, Madeira, Portugal",
                    "currency": "EUR"
                  },
                  {
                    "name": "Rental unit in Funchal · ★4.85 · 2 bedrooms · 3 beds · 2 bath",
                    "price": 110,
                    "pricing_details": {
                      "num_of_nights": 1,
                      "initial_price_per_night": 110,
                      "price_per_night": 110,
                      "price_without_fees": 110
                    },
                    "location": "Funchal, Madeira, Portugal",
                    "currency": "EUR"
                  },
                  {
                    "name": "Rental unit in Funchal · ★4.60 · 2 bedrooms · 4 beds · 1 bath",
                    "price": 80,
                    "pricing_details": {
                      "num_of_nights": 1,
                      "initial_price_per_night": 80,
                      "price_per_night": 80,
                      "price_without_fees": 80
                    },
                    "location": "Funchal, Madeira, Portugal",
                    "currency": "EUR"
                  }
                ]
                """;

        Property property = new Property();
        property.setSize(80);
        property.setBedrooms(2);
        property.setWc(1);
        property.setCountry("Portugal");
        property.setCity("Funchal");
        property.setAddress("Rua X");
        property.setMortgage(800);
        property.setUtilities(150);
        property.setCashInvested(70000);
        property.setPool(true);
        property.setGarden(false);
        property.setParking(true);
        property.setId(1L);

        when(brigthdataService.scrapeShortRentalPlatforms(any(Platform.class),isNull(), any(String.class))).thenReturn(resultJsonAirbnb);
        when(propertyService.getProperty(any(Long.class))).thenReturn(property);
        when(occupancyRateConfig.getOccupancyRate(any(String.class))).thenReturn(0.72);

        ProfitabilityResponse response = profitabilityService.getProfitability(profitabilityRequest);

        assertEquals(2052.0, response.estimatedMonthlyRevenue(), 0.1);
        assertEquals(24624.0, response.estimatedYearlyRevenue(), 0.1);
        assertEquals(13.26, response.ROI(), 0.1);
        assertNotNull(response.result());
        assertTrue(response.result().contains("70000"));
        assertTrue(response.result().contains("2052"));
    }

    @Test
    void generateLongProfitability_shouldReturnProfitabilityResponse() {

        ProfitabilityRequest request = new ProfitabilityRequest(1L, RentalType.LONG, Platform.AIRBNB, 16, "Euro");

        Property property = new Property();
        property.setId(1L);
        property.setSize(80);
        property.setBedrooms(2);
        property.setWc(1);
        property.setCountry("Portugal");
        property.setCity("Funchal");
        property.setAddress("Rua X");
        property.setMortgage(800);
        property.setUtilities(150);
        property.setCashInvested(70000);
        property.setPool(true);
        property.setGarden(false);
        property.setParking(true);

        when(propertyService.getProperty(any(Long.class))).thenReturn(property);
        when(openAIService.callOpenAI(any(String.class))).thenReturn("1200");

        ProfitabilityResponse response = profitabilityService.getProfitability(request);

        assertEquals(1200.0, response.estimatedMonthlyRevenue(), 0.1);
        assertEquals(14400.0, response.estimatedYearlyRevenue(), 0.1);
        assertEquals(0.994, response.ROI(), 0.1);
        assertNotNull(response.result());
    }

    @Test
    void compareShortAndLongProfitability_shouldReturnBothResponses() {

        ProfitabilityRequest request = new ProfitabilityRequest(1L, RentalType.SHORT, Platform.ALL, 16, "Euro");

        String resultJsonAirbnb = """
            [
              {"pricing_details": {"price_per_night": 95}},
              {"pricing_details": {"price_per_night": 110}},
              {"pricing_details": {"price_per_night": 80}}
            ]
            """;

        String resultJsonBooking = """
            [
              {"original_price": 90},
              {"original_price": 105},
              {"original_price": 120}
            ]
            """;

        Property property = new Property();
        property.setId(1L);
        property.setSize(80);
        property.setBedrooms(2);
        property.setWc(1);
        property.setCountry("Portugal");
        property.setCity("Funchal");
        property.setAddress("Rua X");
        property.setMortgage(800);
        property.setUtilities(150);
        property.setCashInvested(70000);
        property.setPool(true);
        property.setGarden(false);
        property.setParking(true);

        when(propertyService.getProperty(any(Long.class))).thenReturn(property);
        when(openAIService.callOpenAI(any(String.class))).thenReturn("1200");
        when(occupancyRateConfig.getOccupancyRate(any(String.class))).thenReturn(0.72);
        when(countryCodeConfig.getCountryCode(any(String.class))).thenReturn("PT");
        when(brigthdataService.scrapeShortRentalPlatforms(any(Platform.class),isNull(), contains("num_of_adults"))).thenReturn(resultJsonAirbnb);
        when(brigthdataService.scrapeShortRentalPlatforms(any(Platform.class), isNull(), contains("booking.com"))).thenReturn(resultJsonBooking);

        ProfitabilityCompareResponse response = profitabilityService.getCompareProfitability(request);

        // short rental assertions (Airbnb + Booking average)
        assertNotNull(response.shortRental());
        assertEquals(2160.0, response.shortRental().estimatedMonthlyRevenue(), 0.1);
        assertEquals(14.82, response.shortRental().ROI(), 0.1);

        // long rental assertions (OpenAI)
        assertNotNull(response.longRental());
        assertEquals(1200.0, response.longRental().estimatedMonthlyRevenue(), 0.1);
        assertEquals(0.994, response.longRental().ROI(), 0.1);
    }
}
