package com.lll.futures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_token_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String walletAddress;
    
    @Column(nullable = false)
    @Builder.Default
    private Double lllBalance = 0.0;
    
    @Column(nullable = false)
    @Builder.Default
    private Double stakedAmount = 0.0;
    
    @Column(nullable = false)
    @Builder.Default
    private Double totalEarned = 0.0;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        lastUpdated = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
