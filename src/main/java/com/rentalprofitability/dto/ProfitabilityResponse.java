package com.rentalprofitability.dto;

import com.rentalprofitability.model.RentalType;

public record ProfitabilityResponse(Long propertyID , RentalType rentaltype, double estimatedMonthlyRevenue, double estimatedYearlyRevenue, double netMonthlyProfit, double netYearlyProfit, double ROI, String result) {
}
