package com.lll.futures.dto;

import com.lll.futures.model.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Wallet address is required")
    private String walletAddress;
    
    @NotNull(message = "Market ID is required")
    private Long marketId;
    
    @NotNull(message = "Order side is required")
    private Order.OrderSide side;
    
    @NotNull(message = "Stake amount is required")
    @Positive(message = "Stake amount must be positive")
    private Double stakeAmount;
}


