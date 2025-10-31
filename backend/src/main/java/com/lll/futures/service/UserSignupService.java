package com.lll.futures.service;

import com.lll.futures.dto.UserSignupRequest;
import com.lll.futures.dto.UserSignupResponse;
import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class UserSignupService {
    
    private final UserRepository userRepository;
    private final TokenSyncService tokenSyncService;
    private final WalletService walletService;
    private final RewardDistributionService rewardDistributionService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public UserSignupService(UserRepository userRepository, 
                           TokenSyncService tokenSyncService,
                           WalletService walletService,
                           RewardDistributionService rewardDistributionService) {
        this.userRepository = userRepository;
        this.tokenSyncService = tokenSyncService;
        this.walletService = walletService;
        this.rewardDistributionService = rewardDistributionService;
    }
    
    @Transactional
    public UserSignupResponse signupUser(UserSignupRequest request) {
        log.info("Creating new user: {}", request.getUsername());
        
        // Generate a unique wallet address for the user
        String walletAddress = generateWalletAddress();
        
        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash password
                .walletAddress(walletAddress)
                .tokenBalance(50.0) // New users get 50 real LLL tokens
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        // Create real Solana wallet for the user
        try {
            var userWallet = walletService.createUserWallet(savedUser.getId());
            log.info("Created real Solana wallet for user {} with public key: {}", 
                savedUser.getUsername(), userWallet.getPublicKey());
            
            // Update user with real wallet address
            savedUser.setWalletAddress(userWallet.getPublicKey());
            userRepository.save(savedUser);
            log.info("Updated user {} with real wallet address: {}", 
                savedUser.getUsername(), userWallet.getPublicKey());
        } catch (Exception e) {
            log.error("Failed to create wallet for user {}: {}", savedUser.getUsername(), e.getMessage());
            // Continue anyway - user is created
        }
        
        // Queue signup bonus reward for distribution
        try {
            rewardDistributionService.queueReward(savedUser.getId(), 50.0, "signup_bonus");
            log.info("Queued signup bonus reward for user {}", savedUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to queue reward for user {}: {}", savedUser.getUsername(), e.getMessage());
        }
        
        // Sync user with token balance system
        tokenSyncService.syncUserToWallet(savedUser);
        
        // Create response
        return UserSignupResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .tokenBalance(savedUser.getTokenBalance())
                .walletAddress(savedUser.getWalletAddress())
                .createdAt(savedUser.getCreatedAt())
                .message("Welcome! You've received 50 LLL tokens to start trading.")
                .build();
    }
    
    private String generateWalletAddress() {
        // Generate a unique wallet address for the User model
        // The actual Solana keypair is created by WalletService
        return "User_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
