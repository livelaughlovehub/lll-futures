package com.lll.futures.controller;

import com.lll.futures.dto.CreateMarketRequest;
import com.lll.futures.dto.MarketDTO;
import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.service.MarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketController {
    
    private final MarketService marketService;
    private final UserRepository userRepository;
    
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
        // Get current user from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Override creatorId with authenticated user's ID for security
        request.setCreatorId(currentUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marketService.createMarket(request));
    }
    
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarketDTO> closeMarket(@PathVariable Long id) {
        return ResponseEntity.ok(marketService.closeMarket(id));
    }
}


