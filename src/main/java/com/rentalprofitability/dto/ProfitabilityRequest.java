package com.rentalprofitability.dto;

import com.rentalprofitability.model.Platform;
import com.rentalprofitability.model.RentalType;

public record ProfitabilityRequest(Long propertyID , RentalType rentaltype, Platform platform, double propertyManagementFee ) {
}
