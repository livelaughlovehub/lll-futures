package com.lll.futures.controller;

import com.lll.futures.dto.MarketDTO;
import com.lll.futures.dto.SettleMarketRequest;
import com.lll.futures.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {
    
    private final SettlementService settlementService;
    
    @PostMapping("/settle")
    public ResponseEntity<MarketDTO> settleMarket(@Valid @RequestBody SettleMarketRequest request) {
        return ResponseEntity.ok(settlementService.settleMarket(request));
    }
}


