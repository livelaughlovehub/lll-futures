package com.lll.futures.service;

import com.lll.futures.model.StakingRecord;
import com.lll.futures.model.TradingReward;
import com.lll.futures.model.UserTokenBalance;
import com.lll.futures.repository.StakingRecordRepository;
import com.lll.futures.repository.TradingRewardRepository;
import com.lll.futures.repository.UserTokenBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLLTokenService {
    
    private final SolanaService solanaService;
    private final UserTokenBalanceRepository tokenBalanceRepository;
    private final TradingRewardRepository tradingRewardRepository;
    private final StakingRecordRepository stakingRecordRepository;
    
    /**
     * Get token balance information for a wallet
     */
    @Transactional(readOnly = true)
    public UserTokenBalance getTokenBalance(String walletAddress) {
        Optional<UserTokenBalance> existing = tokenBalanceRepository.findByWalletAddress(walletAddress);
        
        if (existing.isPresent()) {
            // Update balance from blockchain
            UserTokenBalance balance = existing.get();
            Double blockchainBalance = solanaService.getTokenBalance(walletAddress);
            balance.setLllBalance(blockchainBalance);
            balance.setLastUpdated(LocalDateTime.now());
            return tokenBalanceRepository.save(balance);
        } else {
            // Create new balance record
            Double blockchainBalance = solanaService.getTokenBalance(walletAddress);
            UserTokenBalance newBalance = UserTokenBalance.builder()
                    .walletAddress(walletAddress)
                    .lllBalance(blockchainBalance)
                    .stakedAmount(0.0)
                    .totalEarned(0.0)
                    .build();
            return tokenBalanceRepository.save(newBalance);
        }
    }
    
    /**
     * Stake tokens
     */
    @Transactional
    public StakingRecord stakeTokens(String walletAddress, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Stake amount must be positive");
        }
        
        UserTokenBalance balance = getTokenBalance(walletAddress);
        if (balance.getLllBalance() < amount) {
            throw new RuntimeException("Insufficient LLL balance. Available: " + balance.getLllBalance());
        }
        
        // Call Solana smart contract
        String txSignature = solanaService.stakeTokens(walletAddress, amount);
        
        // Update local balance
        balance.setLllBalance(balance.getLllBalance() - amount);
        balance.setStakedAmount(balance.getStakedAmount() + amount);
        tokenBalanceRepository.save(balance);
        
        // Record staking transaction
        StakingRecord record = StakingRecord.builder()
                .walletAddress(walletAddress)
                .amount(amount)
                .action(StakingRecord.StakingAction.STAKE)
                .transactionSignature(txSignature)
                .build();
        
        StakingRecord savedRecord = stakingRecordRepository.save(record);
        log.info("Staked {} LLL tokens for wallet: {}", amount, walletAddress);
        
        return savedRecord;
    }
    
    /**
     * Unstake tokens
     */
    @Transactional
    public StakingRecord unstakeTokens(String walletAddress, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Unstake amount must be positive");
        }
        
        UserTokenBalance balance = getTokenBalance(walletAddress);
        if (balance.getStakedAmount() < amount) {
            throw new RuntimeException("Insufficient staked amount. Available: " + balance.getStakedAmount());
        }
        
        // Call Solana smart contract
        String txSignature = solanaService.unstakeTokens(walletAddress, amount);
        
        // Update local balance
        balance.setLllBalance(balance.getLllBalance() + amount);
        balance.setStakedAmount(balance.getStakedAmount() - amount);
        tokenBalanceRepository.save(balance);
        
        // Record unstaking transaction
        StakingRecord record = StakingRecord.builder()
                .walletAddress(walletAddress)
                .amount(amount)
                .action(StakingRecord.StakingAction.UNSTAKE)
                .transactionSignature(txSignature)
                .build();
        
        StakingRecord savedRecord = stakingRecordRepository.save(record);
        log.info("Unstaked {} LLL tokens for wallet: {}", amount, walletAddress);
        
        return savedRecord;
    }
    
    /**
     * Claim trading reward
     */
    @Transactional
    public TradingReward claimReward(String walletAddress, Double amount, TradingReward.RewardType rewardType) {
        if (amount <= 0) {
            throw new RuntimeException("Reward amount must be positive");
        }
        
        // Check if user already claimed this type of reward today
        if (rewardType == TradingReward.RewardType.DAILY_LOGIN) {
            List<TradingReward> todayRewards = tradingRewardRepository
                    .findByWalletAddressAndRewardTypeAndDate(walletAddress, rewardType, LocalDateTime.now());
            if (!todayRewards.isEmpty()) {
                throw new RuntimeException("Daily login reward already claimed today");
            }
        }
        
        // Call Solana smart contract
        String txSignature = solanaService.distributeReward(walletAddress, amount, rewardType.name());
        
        // Update user balance
        UserTokenBalance balance = getTokenBalance(walletAddress);
        balance.setLllBalance(balance.getLllBalance() + amount);
        balance.setTotalEarned(balance.getTotalEarned() + amount);
        tokenBalanceRepository.save(balance);
        
        // Record reward
        TradingReward reward = TradingReward.builder()
                .walletAddress(walletAddress)
                .rewardAmount(amount)
                .rewardType(rewardType)
                .transactionSignature(txSignature)
                .build();
        
        TradingReward savedReward = tradingRewardRepository.save(reward);
        log.info("Claimed {} LLL reward ({}) for wallet: {}", amount, rewardType, walletAddress);
        
        return savedReward;
    }
    
    /**
     * Get staking information
     */
    @Transactional(readOnly = true)
    public Object getStakingInfo(String walletAddress) {
        UserTokenBalance balance = getTokenBalance(walletAddress);
        
        // Get staking info from Solana
        Object solanaStakingInfo = solanaService.getStakingInfo(walletAddress);
        
        // Get staking history
        List<StakingRecord> stakingHistory = stakingRecordRepository.findByWalletAddress(walletAddress);
        
        return Map.of(
            "walletAddress", walletAddress,
            "stakedAmount", balance.getStakedAmount(),
            "lllBalance", balance.getLllBalance(),
            "totalEarned", balance.getTotalEarned(),
            "stakingHistory", stakingHistory,
            "solanaInfo", solanaStakingInfo
        );
    }
    
    /**
     * Process daily login reward
     */
    @Transactional
    public TradingReward processDailyLogin(String walletAddress) {
        return claimReward(walletAddress, 5.0, TradingReward.RewardType.DAILY_LOGIN);
    }
    
    /**
     * Process trading reward
     */
    @Transactional
    public TradingReward processTradingReward(String walletAddress, Double tradeAmount) {
        Double rewardAmount = tradeAmount * 0.01; // 1% of trade amount
        return claimReward(walletAddress, rewardAmount, TradingReward.RewardType.TRADING);
    }
    
    /**
     * Process profitable trade bonus
     */
    @Transactional
    public TradingReward processProfitableTradeBonus(String walletAddress, Double profit) {
        Double bonusAmount = profit * 0.1; // 10% of profit
        return claimReward(walletAddress, bonusAmount, TradingReward.RewardType.TRADING);
    }
}
