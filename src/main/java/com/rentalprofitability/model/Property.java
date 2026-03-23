package com.rentalprofitability.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int size;
    private double bedrooms;
    private double wc;
    private String country;
    private String city;

    @Column(columnDefinition = "TEXT")
    private String address;

    @NotNull
    @Positive
    private double mortgage;

    @NotNull
    @Positive
    private double utilities;

    @NotNull
    @Positive
    private double cashInvested;

    private boolean pool;
    private boolean garden;
    private boolean parking;


}
