package com.lll.futures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(length = 500)
    private String reason;  // "signup_bonus", "trading_reward", "referral", etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardStatus status;
    
    @Column(length = 100)
    private String transactionSignature;  // Solana TX signature when completed
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(length = 500)
    private String errorMessage;  // If distribution fails
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RewardStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum RewardStatus {
        PENDING,      // Waiting to be processed
        PROCESSING,   // Currently being distributed
        COMPLETED,    // Successfully sent
        FAILED        // Failed to distribute
    }
}

