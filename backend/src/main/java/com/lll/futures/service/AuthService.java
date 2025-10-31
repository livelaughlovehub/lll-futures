package com.lll.futures.service;

import com.lll.futures.dto.JwtResponse;
import com.lll.futures.dto.SignInRequest;
import com.lll.futures.dto.UserDTO;
import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public JwtResponse signIn(SignInRequest request) {
        log.info("Sign in attempt for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        log.info("User {} signed in successfully", user.getUsername());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getUsername(), 
                user.getId(), 
                user.getIsAdmin()
        );
        
        // Get user DTO
        UserDTO userDTO = userService.getUserById(user.getId());
        
        // Return JWT response
        return JwtResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }
    
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
}
