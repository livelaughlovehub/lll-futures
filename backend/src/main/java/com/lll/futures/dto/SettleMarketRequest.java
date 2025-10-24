package com.lll.futures.dto;

import com.lll.futures.model.Market;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettleMarketRequest {
    
    @NotNull(message = "Market ID is required")
    private Long marketId;
    
    @NotNull(message = "Outcome is required")
    private Market.MarketOutcome outcome;
}


