package com.lll.futures.service;

import com.lll.futures.model.User;
import com.lll.futures.model.UserTokenBalance;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.repository.UserTokenBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service to sync token balances between User and UserTokenBalance models
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenSyncService {
    
    private final UserRepository userRepository;
    private final UserTokenBalanceRepository userTokenBalanceRepository;
    private final SolanaService solanaService;
    
    /**
     * Sync token balance from User to UserTokenBalance
     */
    @Transactional
    public void syncUserToWallet(User user) {
        if (user.getWalletAddress() == null) {
            log.warn("User {} has no wallet address, skipping sync", user.getUsername());
            return;
        }
        
        Optional<UserTokenBalance> existingBalance = userTokenBalanceRepository
            .findByWalletAddress(user.getWalletAddress());
        
        if (existingBalance.isPresent()) {
            UserTokenBalance balance = existingBalance.get();
            balance.setLllBalance(user.getTokenBalance());
            userTokenBalanceRepository.save(balance);
            log.debug("Synced user {} balance to wallet {}: {} LLL", 
                user.getUsername(), user.getWalletAddress(), user.getTokenBalance());
        } else {
            // Create new UserTokenBalance record
            UserTokenBalance newBalance = UserTokenBalance.builder()
                .walletAddress(user.getWalletAddress())
                .lllBalance(user.getTokenBalance())
                .stakedAmount(0.0)
                .totalEarned(0.0)
                .build();
            userTokenBalanceRepository.save(newBalance);
            log.info("Created new wallet balance for user {}: {} LLL", 
                user.getUsername(), user.getWalletAddress());
        }
    }
    
    /**
     * Sync token balance from UserTokenBalance to User
     */
    @Transactional
    public void syncWalletToUser(String walletAddress) {
        Optional<UserTokenBalance> walletBalance = userTokenBalanceRepository
            .findByWalletAddress(walletAddress);
        
        if (walletBalance.isEmpty()) {
            log.warn("No wallet balance found for address: {}", walletAddress);
            return;
        }
        
        Optional<User> user = userRepository.findByWalletAddress(walletAddress);
        if (user.isEmpty()) {
            log.warn("No user found for wallet address: {}", walletAddress);
            return;
        }
        
        User userEntity = user.get();
        UserTokenBalance balance = walletBalance.get();
        
        userEntity.setTokenBalance(balance.getLllBalance());
        userRepository.save(userEntity);
        
        log.debug("Synced wallet {} balance to user {}: {} LLL", 
            walletAddress, userEntity.getUsername(), balance.getLllBalance());
    }
    
    /**
     * Assign a real Solana wallet address to a user
     */
    @Transactional
    public void assignRealWalletAddress(User user, String realWalletAddress) {
        // Validate wallet address format (Solana addresses are 32-44 characters)
        if (!isValidSolanaAddress(realWalletAddress)) {
            throw new IllegalArgumentException("Invalid Solana wallet address: " + realWalletAddress);
        }
        
        user.setWalletAddress(realWalletAddress);
        userRepository.save(user);
        
        // Get real balance from blockchain
        Double realBalance = solanaService.getTokenBalance(realWalletAddress);
        
        // Create or update UserTokenBalance with real wallet
        Optional<UserTokenBalance> existingBalance = userTokenBalanceRepository
            .findByWalletAddress(realWalletAddress);
        
        if (existingBalance.isPresent()) {
            UserTokenBalance balance = existingBalance.get();
            balance.setLllBalance(realBalance);
            userTokenBalanceRepository.save(balance);
        } else {
            UserTokenBalance newBalance = UserTokenBalance.builder()
                .walletAddress(realWalletAddress)
                .lllBalance(realBalance)
                .stakedAmount(0.0)
                .totalEarned(0.0)
                .build();
            userTokenBalanceRepository.save(newBalance);
        }
        
        // Update user's token balance to match blockchain
        user.setTokenBalance(realBalance);
        userRepository.save(user);
        
        log.info("Assigned real wallet address {} to user {} with real balance {} LLL", 
            realWalletAddress, user.getUsername(), realBalance);
    }
    
    private boolean isValidSolanaAddress(String address) {
        // Basic Solana address validation (32-44 characters, base58)
        return address != null && address.length() >= 32 && address.length() <= 44;
    }
    
    /**
     * Assign a wallet address to a user if they don't have one
     */
    @Transactional
    public void assignWalletAddress(User user) {
        if (user.getWalletAddress() != null) {
            log.debug("User {} already has wallet address: {}", user.getUsername(), user.getWalletAddress());
            return;
        }
        
        // Generate a mock wallet address for demo purposes
        String mockWalletAddress = generateMockWalletAddress(user.getId());
        user.setWalletAddress(mockWalletAddress);
        userRepository.save(user);
        
        // Create corresponding UserTokenBalance record
        UserTokenBalance balance = UserTokenBalance.builder()
            .walletAddress(mockWalletAddress)
            .lllBalance(user.getTokenBalance())
            .stakedAmount(0.0)
            .totalEarned(0.0)
            .build();
        userTokenBalanceRepository.save(balance);
        
        log.info("Assigned wallet address {} to user {} with balance {} LLL", 
            mockWalletAddress, user.getUsername(), user.getTokenBalance());
    }
    
    /**
     * Generate a mock wallet address for demo purposes
     */
    private String generateMockWalletAddress(Long userId) {
        return String.format("DemoWallet%d%s", userId, 
            java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12));
    }
    
    /**
     * Sync all users to their wallet balances
     */
    @Transactional
    public void syncAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getWalletAddress() == null) {
                assignWalletAddress(user);
            } else {
                syncUserToWallet(user);
            }
        }
        log.info("Synced {} users to their wallet balances", users.size());
    }
    
    /**
     * Get user by wallet address
     */
    public Optional<User> getUserByWalletAddress(String walletAddress) {
        return userRepository.findByWalletAddress(walletAddress);
    }
    
    /**
     * Get wallet balance by user ID
     */
    public Optional<UserTokenBalance> getWalletBalanceByUserId(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && user.get().getWalletAddress() != null) {
            return userTokenBalanceRepository.findByWalletAddress(user.get().getWalletAddress());
        }
        return Optional.empty();
    }
}
