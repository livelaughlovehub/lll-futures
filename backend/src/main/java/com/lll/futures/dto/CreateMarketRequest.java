package com.lll.futures.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMarketRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;
    
    @NotNull(message = "Yes odds are required")
    @Positive(message = "Yes odds must be positive")
    private Double yesOdds;
    
    @NotNull(message = "No odds are required")
    @Positive(message = "No odds must be positive")
    private Double noOdds;
    
    @NotNull(message = "Creator ID is required")
    private Long creatorId;
}


