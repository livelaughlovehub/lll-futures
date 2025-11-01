package com.lll.futures.config;

import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.security.JwtUtil;
import com.lll.futures.service.WalletService;
import com.lll.futures.service.RewardDistributionService;
import com.lll.futures.service.TokenSyncService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WalletService walletService;
    private final RewardDistributionService rewardDistributionService;
    private final TokenSyncService tokenSyncService;
    
    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        
        log.info("OAuth2 login successful for: {}", email);
        
        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create new user from OAuth
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email.split("@")[0] + "_" + System.currentTimeMillis());
                    newUser.setBio(name); // Store name in bio field
                    newUser.setProfilePicture(picture);
                    newUser.setPassword(""); // OAuth users don't need password
                    newUser.setIsAdmin(false);
                    newUser.setTokenBalance(50.0); // New users get 50 LLL tokens
                    newUser = userRepository.save(newUser);
                    
                    // Create real Solana wallet for OAuth user
                    try {
                        var userWallet = walletService.createUserWallet(newUser.getId());
                        log.info("Created real Solana wallet for OAuth user {} with public key: {}", 
                            newUser.getUsername(), userWallet.getPublicKey());
                        
                        // Update user with real wallet address
                        newUser.setWalletAddress(userWallet.getPublicKey());
                        userRepository.save(newUser);
                    } catch (Exception e) {
                        log.error("Failed to create wallet for OAuth user {}: {}", 
                            newUser.getUsername(), e.getMessage());
                    }
                    
                    // Queue signup bonus reward for OAuth user
                    try {
                        rewardDistributionService.queueReward(newUser.getId(), 50.0, "signup_bonus");
                        log.info("Queued signup bonus reward for OAuth user {}", newUser.getUsername());
                    } catch (Exception e) {
                        log.error("Failed to queue reward for OAuth user {}: {}", 
                            newUser.getUsername(), e.getMessage());
                    }
                    
                    // Sync user with token balance system
                    try {
                        tokenSyncService.syncUserToWallet(newUser);
                    } catch (Exception e) {
                        log.error("Failed to sync OAuth user to wallet: {}", e.getMessage());
                    }
                    
                    return newUser;
                });
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getIsAdmin());
        
        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/auth/callback?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

