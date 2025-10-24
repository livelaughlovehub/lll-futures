package com.lll.futures.controller;

import com.lll.futures.dto.CreateMarketRequest;
import com.lll.futures.dto.MarketDTO;
import com.lll.futures.service.MarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketController {
    
    private final MarketService marketService;
    
    @GetMapping
    public ResponseEntity<List<MarketDTO>> getAllMarkets() {
        return ResponseEntity.ok(marketService.getAllMarkets());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<MarketDTO>> getActiveMarkets() {
        return ResponseEntity.ok(marketService.getActiveMarkets());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MarketDTO> getMarketById(@PathVariable Long id) {
        return ResponseEntity.ok(marketService.getMarketById(id));
    }
    
    @PostMapping
    public ResponseEntity<MarketDTO> createMarket(@Valid @RequestBody CreateMarketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marketService.createMarket(request));
    }
    
    @PutMapping("/{id}/close")
    public ResponseEntity<MarketDTO> closeMarket(@PathVariable Long id) {
        return ResponseEntity.ok(marketService.closeMarket(id));
    }
}


