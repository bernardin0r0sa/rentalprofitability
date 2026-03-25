package com.rentalprofitability.dto;

import com.rentalprofitability.model.RentalType;

public record ProfitabilityResponse(Long propertyID , RentalType rentaltype, double estimatedMonthlyRevenue, double estimatedYearlyRevenue, double estimatedNetMonthlyProfit, double estimatedNetYearlyProfit, double ROI, String result) {
}
