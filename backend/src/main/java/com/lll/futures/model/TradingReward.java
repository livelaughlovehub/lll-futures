package com.lll.futures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trading_rewards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingReward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String walletAddress;
    
    @Column
    private Long tradeId;
    
    @Column(nullable = false)
    private Double rewardAmount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RewardType rewardType;
    
    @Column
    private String transactionSignature;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum RewardType {
        DAILY_LOGIN, SOCIAL_INTERACTION, TRADING, CHALLENGE
    }
}
