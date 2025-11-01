package com.lll.futures.service;

import com.lll.futures.dto.OrderDTO;
import com.lll.futures.dto.PlaceOrderRequest;
import com.lll.futures.model.Market;
import com.lll.futures.model.Order;
import com.lll.futures.model.Transaction;
import com.lll.futures.model.User;
import com.lll.futures.model.UserTokenBalance;
import com.lll.futures.repository.MarketRepository;
import com.lll.futures.repository.OrderRepository;
import com.lll.futures.repository.TransactionRepository;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.repository.UserTokenBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final TransactionRepository transactionRepository;
    private final UserTokenBalanceRepository userTokenBalanceRepository;
    private final UserService userService;
    private final MarketService marketService;
    private final LLLTokenService lllTokenService;
    private final SolanaService solanaService;
    private final WalletService walletService;
    private final VaultService vaultService;
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOpenOrders(Long userId) {
        return orderRepository.findByUserIdAndStatus(userId, Order.OrderStatus.OPEN).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToDTO(order);
    }
    
    @Transactional
    public OrderDTO placeOrder(PlaceOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        
        Market market = marketRepository.findById(request.getMarketId())
                .orElseThrow(() -> new RuntimeException("Market not found with id: " + request.getMarketId()));
        
        if (market.getStatus() != Market.MarketStatus.ACTIVE) {
            throw new RuntimeException("Market is not active");
        }
        
        // Check if user has sufficient tokens from their User model
        if (user.getTokenBalance() < request.getStakeAmount()) {
            throw new RuntimeException("Insufficient LLL tokens. Available: " + user.getTokenBalance() + 
                    " LLL, Required: " + request.getStakeAmount() + " LLL");
        }
        
        Double odds = request.getSide() == Order.OrderSide.YES ? market.getYesOdds() : market.getNoOdds();
        Double potentialPayout = request.getStakeAmount() * odds;
        
        Order order = Order.builder()
                .user(user)
                .walletAddress(request.getWalletAddress())
                .market(market)
                .side(request.getSide())
                .stakeAmount(request.getStakeAmount())
                .odds(odds)
                .potentialPayout(potentialPayout)
                .status(Order.OrderStatus.OPEN)
                .build();
        
        order = orderRepository.save(order);
        
        // Deduct from user's token balance
        Double newBalance = user.getTokenBalance() - request.getStakeAmount();
        user.setTokenBalance(newBalance);
        userRepository.save(user);
        
        log.debug("Deducted {} LLL from user balance. New balance: {}", request.getStakeAmount(), newBalance);
        
        // Transfer tokens from user wallet to vault (escrow)
        try {
            String vaultPublicKey = vaultService.getVaultPublicKey();
            String userWalletAddress = request.getWalletAddress();
            
            String txSignature = solanaService.transferSPLTokenFromUserWallet(
                getUserWalletKeypair(user.getId()), 
                userWalletAddress, 
                vaultPublicKey, 
                request.getStakeAmount()
            );
            
            log.info("Transferred {} LLL from user {} to vault (escrow) - TX: {}", 
                request.getStakeAmount(), userWalletAddress, txSignature);
        } catch (Exception e) {
            log.error("Failed to transfer tokens to vault: {}", e.getMessage());
            // Continue even if transfer fails - user balance already deducted
            // In production, you might want to rollback the order
        }
        
        // Update market volume
        marketService.updateMarketVolume(market.getId(), request.getStakeAmount(), 
                request.getSide() == Order.OrderSide.YES);
        
        // Create transaction record
        createTransaction(user, Transaction.TransactionType.BET_PLACED, 
                -request.getStakeAmount(), 
                "Bet placed on: " + market.getTitle(),
                order.getId(), market.getId());
        
        log.info("Order placed: User {} bet {} LLL on {} for market: {}", 
                user.getUsername(), request.getStakeAmount(), request.getSide(), market.getTitle());
        
        return convertToDTO(order);
    }
    
    private void createTransaction(User user, Transaction.TransactionType type, 
                                   Double amount, String description, 
                                   Long orderId, Long marketId) {
        Double balanceBefore = user.getTokenBalance() - amount;
        
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(user.getTokenBalance())
                .description(description)
                .relatedOrderId(orderId)
                .relatedMarketId(marketId)
                .build();
        
        transactionRepository.save(transaction);
    }
    
    private byte[] getUserWalletKeypair(Long userId) {
        var userWallet = walletService.getUserWallet(userId)
            .orElseThrow(() -> new RuntimeException("User wallet not found for userId: " + userId));
        
        String decryptedPrivateKey = walletService.decryptPrivateKey(userWallet.getEncryptedPrivateKey());
        // Decode Base64 to bytes
        return java.util.Base64.getDecoder().decode(decryptedPrivateKey);
    }
    
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .walletAddress(order.getWalletAddress())
                .marketId(order.getMarket().getId())
                .marketTitle(order.getMarket().getTitle())
                .side(order.getSide())
                .stakeAmount(order.getStakeAmount())
                .odds(order.getOdds())
                .potentialPayout(order.getPotentialPayout())
                .status(order.getStatus())
                .settledAmount(order.getSettledAmount())
                .settledAt(order.getSettledAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
}


