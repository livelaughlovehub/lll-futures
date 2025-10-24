package com.lll.futures.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponseDTO {
    private String transactionSignature;
    private Double rewardAmount;
    private String rewardType;
    private String status;
    private String message;
}
