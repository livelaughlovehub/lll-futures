package com.lll.futures.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBalanceDTO {
    private String walletAddress;
    private Double lllBalance;
    private Double stakedAmount;
    private Double totalEarned;
    private String lastUpdated;
}
