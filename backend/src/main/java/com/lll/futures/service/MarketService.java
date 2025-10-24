package com.lll.futures.service;

import com.lll.futures.dto.CreateMarketRequest;
import com.lll.futures.dto.MarketDTO;
import com.lll.futures.model.Market;
import com.lll.futures.model.User;
import com.lll.futures.repository.MarketRepository;
import com.lll.futures.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {
    
    private final MarketRepository marketRepository;
    private final UserRepository userRepository;
    
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


