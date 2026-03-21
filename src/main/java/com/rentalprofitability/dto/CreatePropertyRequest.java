package com.rentalprofitability.dto;

public record CreatePropertyRequest(
        int size,
        double bedrooms,
        double wc,
        String country,
        String city,
        String address,
        double mortgage,
        double utilities,
        double cashInvested,
        boolean pool,
        boolean garden,
        boolean parking
) {

}