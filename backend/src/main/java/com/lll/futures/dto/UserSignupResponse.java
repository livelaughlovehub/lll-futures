package com.lll.futures.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupResponse {
    private Long id;
    private String username;
    private String email;
    private Double tokenBalance;
    private String walletAddress;
    private LocalDateTime createdAt;
    private String message;
}
