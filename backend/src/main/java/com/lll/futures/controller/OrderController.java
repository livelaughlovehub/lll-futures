package com.lll.futures.controller;

import com.lll.futures.dto.OrderDTO;
import com.lll.futures.dto.PlaceOrderRequest;
import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable Long userId) {
        // Verify user can only see their own orders (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // Get current user ID from token
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
    
    @GetMapping("/user/{userId}/open")
    public ResponseEntity<List<OrderDTO>> getUserOpenOrders(@PathVariable Long userId) {
        // Verify user can only see their own orders (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // Get current user ID from token
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(orderService.getUserOpenOrders(userId));
    }
    
    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }
}


