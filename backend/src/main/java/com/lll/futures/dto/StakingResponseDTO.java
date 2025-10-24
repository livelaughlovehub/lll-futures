package com.lll.futures.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StakingResponseDTO {
    private String transactionSignature;
    private Double amount;
    private String action;
    private String status;
    private String message;
}
