package com.lll.futures.controller;

import com.lll.futures.dto.CreateUserRequest;
import com.lll.futures.dto.UserDTO;
import com.lll.futures.dto.SignInRequest;
import com.lll.futures.dto.UserSignupRequest;
import com.lll.futures.dto.UserSignupResponse;
import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.dto.JwtResponse;
import com.lll.futures.service.AuthService;
import com.lll.futures.service.SolanaService;
import com.lll.futures.service.TokenSyncService;
import com.lll.futures.service.UserService;
import com.lll.futures.service.UserSignupService;
import com.lll.futures.service.WalletService;
import com.lll.futures.model.UserWallet;
import java.util.Base64;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenSyncService tokenSyncService;
    private final UserSignupService userSignupService;
    private final AuthService authService;
    private final WalletService walletService;
    private final SolanaService solanaService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        // Verify user can only access their own data (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // Get current user ID from token
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        // Verify user can only access their own data (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // Get current user's username from token
            String currentUsername = auth.getName();
            
            // Users can only access their own profile
            if (!currentUsername.equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
    
    @GetMapping("/wallet/{walletAddress}")
    public ResponseEntity<UserDTO> getUserByWalletAddress(@PathVariable String walletAddress) {
        // Verify user can only access their own data (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // Get current user's wallet address from token
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Users can only access their own wallet data
            if (currentUser.getWalletAddress() == null || !currentUser.getWalletAddress().equals(walletAddress)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(userService.getUserByWalletAddress(walletAddress));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signupUser(@Valid @RequestBody UserSignupRequest request) {
        try {
            log.info("User signup request for: {}", request.getUsername());
            UserSignupResponse response = userSignupService.signupUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error during user signup: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            log.info("Sign in request for email: {}", request.getEmail());
            JwtResponse response = authService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during sign in: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Get current user's username from JWT token
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user is updating themselves or is admin
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && !currentUser.getId().equals(id)) {
                log.warn("User {} attempted to update user {} profile - FORBIDDEN", username, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("Updating user profile for ID: {} by user: {}", id, username);
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
            
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/assign-wallet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> assignRealWallet(
            @RequestParam Long userId,
            @RequestParam String walletAddress) {
        try {
            // Get the user entity directly from repository
            User user = userService.getUserEntityById(userId);
            tokenSyncService.assignRealWalletAddress(user, walletAddress);
            
            return ResponseEntity.ok(userService.getUserById(userId));
            
        } catch (Exception e) {
            log.error("Error assigning real wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Withdraw tokens from user's app wallet to their personal Phantom wallet
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Map<String, String>> withdrawToPhantom(
            @PathVariable Long id,
            @RequestParam String phantomWallet,
            @RequestParam Double amount) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Verify user can only withdraw from their own account (or is admin)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", "false");
                response.put("message", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                String username = auth.getName();
                User currentUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                if (!currentUser.getId().equals(id)) {
                    response.put("success", "false");
                    response.put("message", "Forbidden: Cannot withdraw from another user's account");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
            }
            
            log.info("Withdrawal request from user {} to phantom wallet: {} amount: {}", 
                id, phantomWallet, amount);
            
            // Validate wallet address
            if (!isValidSolanaAddress(phantomWallet)) {
                response.put("success", "false");
                response.put("message", "Invalid Solana wallet address");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get user
            User user = userService.getUserEntityById(id);
            
            // Check balance
            if (user.getTokenBalance() < amount) {
                response.put("success", "false");
                response.put("message", "Insufficient balance");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get user's app wallet
            var walletOptional = walletService.getUserWallet(id);
            if (walletOptional.isEmpty()) {
                response.put("success", "false");
                response.put("message", "User wallet not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserWallet wallet = walletOptional.get();
            
            // Decrypt user's private key
            String encryptedPrivateKey = wallet.getEncryptedPrivateKey();
            String privateKeyBase64 = walletService.decryptPrivateKey(encryptedPrivateKey);
            byte[] userKeypairBytes = Base64.getDecoder().decode(privateKeyBase64);
            
            log.info("Preparing withdrawal: {} LLL from user wallet {} to {}", 
                amount, wallet.getPublicKey(), phantomWallet);
            
            // Perform real Solana transfer from user's wallet to Phantom wallet
            String transactionSignature;
            try {
                transactionSignature = solanaService.transferSPLTokenFromUserWallet(
                    userKeypairBytes,
                    wallet.getPublicKey(),  // from: user's app wallet
                    phantomWallet,           // to: Phantom wallet
                    amount
                );
                
                log.info("✅ Real withdrawal transaction successful: {}", transactionSignature);
                
            } catch (Exception e) {
                log.error("❌ Error during real withdrawal: {}", e.getMessage(), e);
                response.put("success", "false");
                response.put("message", "Transfer failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            // Update database balance only after successful transfer
            Double newBalance = user.getTokenBalance() - amount;
            user.setTokenBalance(newBalance);
            userRepository.save(user);
            
            log.info("Withdrawal completed: {} LLL from user {} to {} - TX: {}", 
                amount, id, phantomWallet, transactionSignature);
            
            response.put("success", "true");
            response.put("message", "Transfer successful");
            response.put("transaction", transactionSignature);
            response.put("newBalance", String.valueOf(newBalance));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing withdrawal: {}", e.getMessage());
            response.put("success", "false");
            response.put("message", "Withdrawal failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get user's app wallet address for deposits
     */
    @GetMapping("/{id}/deposit-address")
    public ResponseEntity<Map<String, String>> getDepositAddress(@PathVariable Long id) {
        // Verify user can only access their own deposit address (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        Map<String, String> response = new HashMap<>();
        
        try {
            User user = userService.getUserEntityById(id);
            String walletAddress = user.getWalletAddress();
            
            if (walletAddress == null || walletAddress.isEmpty()) {
                response.put("error", "Wallet address not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("walletAddress", walletAddress);
            response.put("tokenMint", solanaService.getTokenMint());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting deposit address for user {}: {}", id, e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check for incoming deposits (optional - can be called periodically by frontend)
     */
    @PostMapping("/{id}/check-deposit")
    public ResponseEntity<Map<String, Object>> checkDeposit(@PathVariable Long id) {
        // Verify user can only check their own deposits (or is admin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserEntityById(id);
            String walletAddress = user.getWalletAddress();
            
            if (walletAddress == null || walletAddress.isEmpty()) {
                response.put("success", false);
                response.put("message", "Wallet address not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get on-chain balance
            Double onChainBalance = solanaService.getTokenBalance(walletAddress);
            
            // Get in-app balance
            Double inAppBalance = user.getTokenBalance();
            
            // Calculate difference (potential deposit)
            Double difference = onChainBalance - inAppBalance;
            
            response.put("success", true);
            response.put("onChainBalance", onChainBalance);
            response.put("inAppBalance", inAppBalance);
            response.put("difference", difference);
            response.put("hasDeposit", difference > 0.01); // Threshold to account for precision
            
            if (difference > 0.01) {
                // Update user balance if deposit detected
                userService.updateBalance(id, difference);
                response.put("message", "Deposit detected! Balance updated.");
                response.put("newBalance", inAppBalance + difference);
            } else {
                response.put("message", "No new deposits detected.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking deposit for user {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    private boolean isValidSolanaAddress(String address) {
        return address != null && address.length() >= 32 && address.length() <= 44;
    }
}


