package com.rentalprofitability.service;

import com.rentalprofitability.dto.CreatePropertyRequest;
import com.rentalprofitability.exception.PropertyNotFoundException;
import com.rentalprofitability.model.Property;
import com.rentalprofitability.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository repo;

    @InjectMocks
    private PropertyService service;

    @Test
    void createProperty_shouldSaveAndReturnProperty() {
        CreatePropertyRequest request = new CreatePropertyRequest(
                80, 2, 1, "Portugal", "Madeira", "Rua X", 800, 150, 70000, true, false, true
        );
        Property saved = new Property();
        saved.setId(1L);
        when(repo.save(any(Property.class))).thenReturn(saved);

        Property result = service.createProperty(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getProperty_whenNotFound_shouldThrowPropertyNotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PropertyNotFoundException.class,
                () -> service.getProperty(99L));
    }
}