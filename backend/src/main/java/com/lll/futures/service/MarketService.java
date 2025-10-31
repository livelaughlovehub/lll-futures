package com.lll.futures.service;

import com.lll.futures.dto.CreateMarketRequest;
import com.lll.futures.dto.MarketDTO;
import com.lll.futures.model.Market;
import com.lll.futures.model.User;
import com.lll.futures.repository.MarketRepository;
import com.lll.futures.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {
    
    private final MarketRepository marketRepository;
    private final UserRepository userRepository;
    
    @Value("${app.market.creation.max-per-day:1}")
    private int maxMarketsPerDay;
    
    @Value("${app.market.creation.min-balance:50.0}")
    private double minBalanceForMarket;
    
    @Transactional(readOnly = true)
    public List<MarketDTO> getAllMarkets() {
        return marketRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MarketDTO> getActiveMarkets() {
        return marketRepository.findByStatus(Market.MarketStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public MarketDTO getMarketById(Long id) {
        Market market = marketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Market not found with id: " + id));
        return convertToDTO(market);
    }
    
    @Transactional
    public MarketDTO createMarket(CreateMarketRequest request) {
        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Creator not found with id: " + request.getCreatorId()));
        
        // Validate market creation limits
        validateMarketCreationLimits(creator);
        
        Market market = Market.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .expiryDate(request.getExpiryDate())
                .yesOdds(request.getYesOdds())
                .noOdds(request.getNoOdds())
                .creator(creator)
                .status(Market.MarketStatus.ACTIVE)
                .totalYesStake(0.0)
                .totalNoStake(0.0)
                .totalVolume(0.0)
                .build();
        
        market = marketRepository.save(market);
        log.info("Created market: {} by {}", market.getTitle(), creator.getUsername());
        
        return convertToDTO(market);
    }
    
    /**
     * Validates that the user can create a market based on configured limits
     */
    private void validateMarketCreationLimits(User creator) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        
        // Check: Maximum markets per day
        List<Market> recentMarkets = marketRepository.findByCreatorAndCreatedAtAfter(creator, oneDayAgo);
        
        if (recentMarkets.size() >= maxMarketsPerDay) {
            Market lastMarket = recentMarkets.stream()
                    .min((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()))
                    .orElse(null);
            
            String timeAgo = lastMarket != null ? getTimeAgo(lastMarket.getCreatedAt()) : "recently";
            throw new RuntimeException(
                String.format("You can only create %d market(s) per 24 hours. " +
                    "Your last market was created %s ago. Please try again later.",
                    maxMarketsPerDay, timeAgo));
        }
        
        // Check: Minimum token balance
        if (creator.getTokenBalance() < minBalanceForMarket) {
            throw new RuntimeException(
                String.format("You need at least %.2f LLL tokens to create a market. " +
                    "Your current balance: %.2f LLL",
                    minBalanceForMarket, creator.getTokenBalance()));
        }
    }
    
    /**
     * Returns a human-readable time difference string
     */
    private String getTimeAgo(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours >= 24) {
            long days = hours / 24;
            return days + (days == 1 ? " day" : " days");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour" : " hours") + 
                   (minutes > 0 ? " and " + minutes + (minutes == 1 ? " minute" : " minutes") : "");
        } else {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
    }
    
    @Transactional
    public void updateMarketVolume(Long marketId, Double amount, boolean isYes) {
        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new RuntimeException("Market not found with id: " + marketId));
        
        market.setTotalVolume(market.getTotalVolume() + amount);
        
        if (isYes) {
            market.setTotalYesStake(market.getTotalYesStake() + amount);
        } else {
            market.setTotalNoStake(market.getTotalNoStake() + amount);
        }
        
        marketRepository.save(market);
        log.debug("Updated market {} volume. Total: {} LLL", marketId, market.getTotalVolume());
    }
    
    @Transactional
    public MarketDTO closeMarket(Long marketId) {
        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new RuntimeException("Market not found with id: " + marketId));
        
        market.setStatus(Market.MarketStatus.CLOSED);
        market = marketRepository.save(market);
        log.info("Closed market: {}", market.getTitle());
        
        return convertToDTO(market);
    }
    
    private MarketDTO convertToDTO(Market market) {
        return MarketDTO.builder()
                .id(market.getId())
                .title(market.getTitle())
                .description(market.getDescription())
                .status(market.getStatus())
                .expiryDate(market.getExpiryDate())
                .yesOdds(market.getYesOdds())
                .noOdds(market.getNoOdds())
                .totalYesStake(market.getTotalYesStake())
                .totalNoStake(market.getTotalNoStake())
                .totalVolume(market.getTotalVolume())
                .creatorId(market.getCreator().getId())
                .creatorUsername(market.getCreator().getUsername())
                .outcome(market.getOutcome())
                .settledAt(market.getSettledAt())
                .createdAt(market.getCreatedAt())
                .build();
    }
}


