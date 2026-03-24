package com.rentalprofitability.service;

import com.rentalprofitability.dto.CreatePropertyRequest;
import com.rentalprofitability.exception.PropertyNotFoundException;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    final PropertyRepository repo;

    public PropertyService(PropertyRepository repo){
        this.repo = repo;
    }

    public Property getProperty(Long id){
     return repo.findById(id).orElseThrow( () -> new PropertyNotFoundException("Property not found: " + id));
    }

    public List<Property> getAllProperties(){
        return repo.findAll();
    }

    public void deleteProperty(Long id){
        repo.deleteById(id);
    }

    public Property createProperty(CreatePropertyRequest request) {
        Property property = new Property();
        property.setSize(request.size());
        property.setBedrooms(request.bedrooms());
        property.setWc(request.wc());
        property.setCountry(request.country());
        property.setCity(request.city());
        property.setAddress(request.address());
        property.setMortgage(request.mortgage());
        property.setUtilities(request.utilities());
        property.setCashInvested(request.cashInvested());
        property.setPool(request.pool());
        property.setGarden(request.garden());
        property.setParking(request.parking());
        return repo.save(property);
    }

    public Property updateProperty(CreatePropertyRequest request , Long id){

        Property property = repo.findById(id).orElseThrow(() -> new PropertyNotFoundException("Property not found: " + id));
        property.setSize(request.size());
        property.setBedrooms(request.bedrooms());
        property.setWc(request.wc());
        property.setCountry(request.country());
        property.setCity(request.city());
        property.setAddress(request.address());
        property.setMortgage(request.mortgage());
        property.setUtilities(request.utilities());
        property.setCashInvested(request.cashInvested());
        property.setPool(request.pool());
        property.setGarden(request.garden());
        property.setParking(request.parking());
        return repo.save(property);
    }


    private void mapRequestToProperty(CreatePropertyRequest request, Property property) {
        property.setSize(request.size());
        property.setBedrooms(request.bedrooms());
        property.setWc(request.wc());
        property.setCountry(request.country());
        property.setCity(request.city());
        property.setAddress(request.address());
        property.setMortgage(request.mortgage());
        property.setUtilities(request.utilities());
        property.setCashInvested(request.cashInvested());
        property.setPool(request.pool());
        property.setGarden(request.garden());
        property.setParking(request.parking());
    }
}
