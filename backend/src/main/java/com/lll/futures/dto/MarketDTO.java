package com.lll.futures.dto;

import com.lll.futures.model.Market;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDTO {
    private Long id;
    private String title;
    private String description;
    private Market.MarketStatus status;
    private LocalDateTime expiryDate;
    private Double yesOdds;
    private Double noOdds;
    private Double totalYesStake;
    private Double totalNoStake;
    private Double totalVolume;
    private Long creatorId;
    private String creatorUsername;
    private Market.MarketOutcome outcome;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
}


