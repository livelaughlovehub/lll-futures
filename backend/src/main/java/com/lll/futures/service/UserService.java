package com.lll.futures.service;

import com.lll.futures.dto.CreateUserRequest;
import com.lll.futures.dto.UserDTO;
import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    @Value("${app.token.initial-balance}")
    private Double initialBalance;
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToDTO(user);
    }
    
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .tokenBalance(request.getInitialBalance() != null ? 
                        request.getInitialBalance() : initialBalance)
                .isAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : false)
                .build();
        
        user = userRepository.save(user);
        log.info("Created user: {} with balance: {} LLL", user.getUsername(), user.getTokenBalance());
        
        return convertToDTO(user);
    }
    
    @Transactional
    public void updateBalance(Long userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Double newBalance = user.getTokenBalance() + amount;
        if (newBalance < 0) {
            throw new RuntimeException("Insufficient balance. Current: " + user.getTokenBalance() + 
                    " LLL, Required: " + Math.abs(amount) + " LLL");
        }
        
        user.setTokenBalance(newBalance);
        userRepository.save(user);
        log.debug("Updated balance for user {}: {} LLL", userId, newBalance);
    }
    
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .tokenBalance(user.getTokenBalance())
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}


