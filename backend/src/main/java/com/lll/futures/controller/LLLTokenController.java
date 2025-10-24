package com.lll.futures.controller;

import com.lll.futures.dto.*;
import com.lll.futures.model.TradingReward;
import com.lll.futures.service.LLLTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lll")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class LLLTokenController {
    
    private final LLLTokenService lllTokenService;
    
    /**
     * Get token balance for a wallet address
     */
    @GetMapping("/balance/{walletAddress}")
    public ResponseEntity<TokenBalanceDTO> getTokenBalance(@PathVariable String walletAddress) {
        try {
            log.info("Getting token balance for wallet: {}", walletAddress);
            
            var balance = lllTokenService.getTokenBalance(walletAddress);
            TokenBalanceDTO dto = TokenBalanceDTO.builder()
                    .walletAddress(balance.getWalletAddress())
                    .lllBalance(balance.getLllBalance())
                    .stakedAmount(balance.getStakedAmount())
                    .totalEarned(balance.getTotalEarned())
                    .lastUpdated(balance.getLastUpdated().toString())
                    .build();
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Error getting token balance for wallet {}: {}", walletAddress, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Stake LLL tokens
     */
    @PostMapping("/stake")
    public ResponseEntity<StakingResponseDTO> stakeTokens(@Valid @RequestBody StakeRequestDTO request) {
        try {
            log.info("Staking {} LLL tokens for wallet: {}", request.getAmount(), request.getWalletAddress());
            
            var stakingRecord = lllTokenService.stakeTokens(request.getWalletAddress(), request.getAmount());
            
            StakingResponseDTO response = StakingResponseDTO.builder()
                    .transactionSignature(stakingRecord.getTransactionSignature())
                    .amount(stakingRecord.getAmount())
                    .action(stakingRecord.getAction().name())
                    .status("SUCCESS")
                    .message("Tokens staked successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error staking tokens for wallet {}: {}", request.getWalletAddress(), e.getMessage());
            
            StakingResponseDTO response = StakingResponseDTO.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Unstake LLL tokens
     */
    @PostMapping("/unstake")
    public ResponseEntity<StakingResponseDTO> unstakeTokens(@Valid @RequestBody UnstakeRequestDTO request) {
        try {
            log.info("Unstaking {} LLL tokens for wallet: {}", request.getAmount(), request.getWalletAddress());
            
            var stakingRecord = lllTokenService.unstakeTokens(request.getWalletAddress(), request.getAmount());
            
            StakingResponseDTO response = StakingResponseDTO.builder()
                    .transactionSignature(stakingRecord.getTransactionSignature())
                    .amount(stakingRecord.getAmount())
                    .action(stakingRecord.getAction().name())
                    .status("SUCCESS")
                    .message("Tokens unstaked successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error unstaking tokens for wallet {}: {}", request.getWalletAddress(), e.getMessage());
            
            StakingResponseDTO response = StakingResponseDTO.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Claim trading reward
     */
    @PostMapping("/rewards/claim")
    public ResponseEntity<RewardResponseDTO> claimReward(@Valid @RequestBody ClaimRewardRequestDTO request) {
        try {
            log.info("Claiming {} LLL reward ({}) for wallet: {}", 
                    request.getAmount(), request.getRewardType(), request.getWalletAddress());
            
            TradingReward.RewardType rewardType = TradingReward.RewardType.valueOf(request.getRewardType());
            var reward = lllTokenService.claimReward(request.getWalletAddress(), request.getAmount(), rewardType);
            
            RewardResponseDTO response = RewardResponseDTO.builder()
                    .transactionSignature(reward.getTransactionSignature())
                    .rewardAmount(reward.getRewardAmount())
                    .rewardType(reward.getRewardType().name())
                    .status("SUCCESS")
                    .message("Reward claimed successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error claiming reward for wallet {}: {}", request.getWalletAddress(), e.getMessage());
            
            RewardResponseDTO response = RewardResponseDTO.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Get staking information for a wallet
     */
    @GetMapping("/staking/{walletAddress}")
    public ResponseEntity<StakingInfoDTO> getStakingInfo(@PathVariable String walletAddress) {
        try {
            log.info("Getting staking info for wallet: {}", walletAddress);
            
            var stakingInfo = lllTokenService.getStakingInfo(walletAddress);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> stakingInfoMap = (Map<String, Object>) stakingInfo;
            
            StakingInfoDTO dto = StakingInfoDTO.builder()
                    .walletAddress(walletAddress)
                    .stakedAmount((Double) stakingInfoMap.get("stakedAmount"))
                    .lllBalance((Double) stakingInfoMap.get("lllBalance"))
                    .totalEarned((Double) stakingInfoMap.get("totalEarned"))
                    .stakingHistory((List<Map<String, Object>>) stakingInfoMap.get("stakingHistory"))
                    .solanaInfo((Map<String, Object>) stakingInfoMap.get("solanaInfo"))
                    .build();
            
            // Extract APY and estimated rewards from Solana info
            @SuppressWarnings("unchecked")
            Map<String, Object> solanaInfo = (Map<String, Object>) stakingInfoMap.get("solanaInfo");
            if (solanaInfo != null) {
                dto.setApy((Double) solanaInfo.get("apy"));
                dto.setEstimatedRewards((Double) solanaInfo.get("estimatedRewards"));
            }
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Error getting staking info for wallet {}: {}", walletAddress, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Process daily login reward
     */
    @PostMapping("/rewards/daily-login")
    public ResponseEntity<RewardResponseDTO> processDailyLogin(@RequestBody Map<String, String> request) {
        try {
            String walletAddress = request.get("walletAddress");
            log.info("Processing daily login reward for wallet: {}", walletAddress);
            
            var reward = lllTokenService.processDailyLogin(walletAddress);
            
            RewardResponseDTO response = RewardResponseDTO.builder()
                    .transactionSignature(reward.getTransactionSignature())
                    .rewardAmount(reward.getRewardAmount())
                    .rewardType(reward.getRewardType().name())
                    .status("SUCCESS")
                    .message("Daily login reward claimed successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing daily login reward: {}", e.getMessage());
            
            RewardResponseDTO response = RewardResponseDTO.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
