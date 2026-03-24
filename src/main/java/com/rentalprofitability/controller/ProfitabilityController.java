package com.rentalprofitability.controller;

import com.rentalprofitability.dto.ProfitabilityCompareResponse;
import com.rentalprofitability.dto.ProfitabilityRequest;
import com.rentalprofitability.dto.ProfitabilityResponse;
import com.rentalprofitability.service.ProfitabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profitability/")
public class ProfitabilityController {

    private final ProfitabilityService profitabilityService;

    public ProfitabilityController(ProfitabilityService profitabilityService) {
        this.profitabilityService = profitabilityService;
    }

    @PostMapping("/short")
    public ResponseEntity<ProfitabilityResponse> getShortProfitability(
            @RequestBody ProfitabilityRequest request) {
        return ResponseEntity.ok(profitabilityService.getProfitability(request));
    }

    @PostMapping("/long")
    public ResponseEntity<ProfitabilityResponse> getLongProfitability(
            @RequestBody ProfitabilityRequest request) {
        return ResponseEntity.ok(profitabilityService.getProfitability(request));
    }

    @PostMapping("/compare")
    public ResponseEntity<ProfitabilityCompareResponse> compare(
            @RequestBody ProfitabilityRequest request) {
        return ResponseEntity.ok(profitabilityService.getCompareProfitability(request));
    }

}
