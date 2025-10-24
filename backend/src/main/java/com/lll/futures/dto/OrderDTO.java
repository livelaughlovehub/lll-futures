package com.lll.futures.dto;

import com.lll.futures.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username;
    private String walletAddress;
    private Long marketId;
    private String marketTitle;
    private Order.OrderSide side;
    private Double stakeAmount;
    private Double odds;
    private Double potentialPayout;
    private Order.OrderStatus status;
    private Double settledAmount;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
}


