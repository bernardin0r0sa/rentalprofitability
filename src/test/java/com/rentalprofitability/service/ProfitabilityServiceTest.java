package com.rentalprofitability.service;


import com.rentalprofitability.dto.CreatePropertyRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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

        when(brigthdataService.scrapeShortRentalPlatforms(isNull(), any(String.class))).thenReturn(resultJsonAirbnb);        when(propertyService.getProperty(any(Long.class))).thenReturn(property);
        when(occupancyRateConfig.getOccupancyRate(any(String.class))).thenReturn(0.72);

        ProfitabilityResponse response = profitabilityService.getProfitability(profitabilityRequest);

        assertEquals(2052.0, response.estimatedMonthlyRevenue(), 0.1);
        assertEquals(24624.0, response.estimatedYearlyRevenue(), 0.1);
        assertEquals(13.26, response.ROI(), 0.1);
        assertNotNull(response.result());
        assertTrue(response.result().contains("70000"));
        assertTrue(response.result().contains("2052"));
    }
}
