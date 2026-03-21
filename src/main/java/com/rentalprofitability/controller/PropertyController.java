package com.rentalprofitability.controller;

import com.rentalprofitability.dto.CreatePropertyRequest;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    PropertyService service;

    @PostMapping
    public ResponseEntity<Property> create(@RequestBody CreatePropertyRequest request)
    {
        Property created = service.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> update(@PathVariable Long id, @RequestBody CreatePropertyRequest request)
    {
        Property updated = service.updateProperty(request,id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id)
    {
        service.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable Long id)
    {
      Property property = service.getProperty(id);
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(property);
    }

    @GetMapping
    public ResponseEntity<List<Property>> getAll()
    {
        List<Property> property = service.getAllProperties();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(property);
    }

}


