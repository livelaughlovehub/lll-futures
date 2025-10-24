package com.lll.futures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "markets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MarketStatus status;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    private Double yesOdds;
    
    @Column(nullable = false)
    private Double noOdds;
    
    @Column(nullable = false)
    private Double totalYesStake;
    
    @Column(nullable = false)
    private Double totalNoStake;
    
    @Column(nullable = false)
    private Double totalVolume;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;
    
    @Column
    @Enumerated(EnumType.STRING)
    private MarketOutcome outcome;
    
    @Column
    private LocalDateTime settledAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = MarketStatus.ACTIVE;
        }
        if (totalYesStake == null) {
            totalYesStake = 0.0;
        }
        if (totalNoStake == null) {
            totalNoStake = 0.0;
        }
        if (totalVolume == null) {
            totalVolume = 0.0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum MarketStatus {
        ACTIVE, CLOSED, SETTLED, CANCELLED
    }
    
    public enum MarketOutcome {
        YES, NO, VOID
    }
}


