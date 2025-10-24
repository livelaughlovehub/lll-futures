package com.lll.futures.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StakingInfoDTO {
    private String walletAddress;
    private Double stakedAmount;
    private Double lllBalance;
    private Double totalEarned;
    private Double estimatedRewards;
    private Double apy;
    private List<Map<String, Object>> stakingHistory;
    private Map<String, Object> solanaInfo;
}
