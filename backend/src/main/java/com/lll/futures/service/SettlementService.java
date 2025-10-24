package com.lll.futures.service;

import com.lll.futures.dto.MarketDTO;
import com.lll.futures.dto.SettleMarketRequest;
import com.lll.futures.model.Market;
import com.lll.futures.model.Order;
import com.lll.futures.model.Transaction;
import com.lll.futures.model.User;
import com.lll.futures.model.UserTokenBalance;
import com.lll.futures.repository.MarketRepository;
import com.lll.futures.repository.OrderRepository;
import com.lll.futures.repository.TransactionRepository;
import com.lll.futures.repository.UserTokenBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {
    
    private final MarketRepository marketRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final UserTokenBalanceRepository userTokenBalanceRepository;
    private final UserService userService;
    private final MarketService marketService;
    
    @Transactional
    public MarketDTO settleMarket(SettleMarketRequest request) {
        Market market = marketRepository.findById(request.getMarketId())
                .orElseThrow(() -> new RuntimeException("Market not found with id: " + request.getMarketId()));
        
        if (market.getStatus() == Market.MarketStatus.SETTLED) {
            throw new RuntimeException("Market already settled");
        }
        
        // Close market first
        market.setStatus(Market.MarketStatus.CLOSED);
        
        // Set outcome
        market.setOutcome(request.getOutcome());
        market.setSettledAt(LocalDateTime.now());
        
        // Get all open orders for this market
        List<Order> openOrders = orderRepository.findByMarketIdAndStatus(
                market.getId(), Order.OrderStatus.OPEN);
        
        log.info("Settling market: {} with outcome: {}. Processing {} orders", 
                market.getTitle(), request.getOutcome(), openOrders.size());
        
        int winnersCount = 0;
        int losersCount = 0;
        Double totalPayouts = 0.0;
        
        for (Order order : openOrders) {
            boolean isWinner = determineWinner(order, request.getOutcome());
            
            // Get LLL token balance for the wallet
            UserTokenBalance tokenBalance = userTokenBalanceRepository.findByWalletAddress(order.getWalletAddress())
                    .orElseThrow(() -> new RuntimeException("No LLL token balance found for wallet: " + order.getWalletAddress()));
            
            if (isWinner) {
                // Pay out winner - add tokens back to staked amount + winnings
                Double payout = order.getPotentialPayout();
                order.setSettledAmount(payout);
                
                // Return staked amount + winnings to staked balance
                tokenBalance.setStakedAmount(tokenBalance.getStakedAmount() + payout);
                userTokenBalanceRepository.save(tokenBalance);
                
                createTransaction(order.getUser(), Transaction.TransactionType.BET_WON, 
                        payout, "Won bet on: " + market.getTitle() + " (Wallet: " + order.getWalletAddress() + ")", 
                        order.getId(), market.getId());
                
                totalPayouts += payout;
                winnersCount++;
                log.debug("Order {} won. Payout: {} LLL to wallet: {}", order.getId(), payout, order.getWalletAddress());
            } else if (request.getOutcome() == Market.MarketOutcome.VOID) {
                // Refund on void - return staked amount
                Double refund = order.getStakeAmount();
                order.setSettledAmount(refund);
                
                // Return staked amount back to staked balance
                tokenBalance.setStakedAmount(tokenBalance.getStakedAmount() + refund);
                userTokenBalanceRepository.save(tokenBalance);
                
                createTransaction(order.getUser(), Transaction.TransactionType.BET_REFUND, 
                        refund, "Refund for voided market: " + market.getTitle() + " (Wallet: " + order.getWalletAddress() + ")", 
                        order.getId(), market.getId());
                
                log.debug("Order {} refunded: {} LLL to wallet: {}", order.getId(), refund, order.getWalletAddress());
            } else {
                // Loser - tokens already deducted when bet was placed
                order.setSettledAmount(0.0);
                
                createTransaction(order.getUser(), Transaction.TransactionType.BET_LOST, 
                        0.0, "Lost bet on: " + market.getTitle() + " (Wallet: " + order.getWalletAddress() + ")", 
                        order.getId(), market.getId());
                
                losersCount++;
                log.debug("Order {} lost, wallet: {}", order.getId(), order.getWalletAddress());
            }
            
            order.setStatus(Order.OrderStatus.SETTLED);
            order.setSettledAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        market.setStatus(Market.MarketStatus.SETTLED);
        market = marketRepository.save(market);
        
        log.info("Market {} settled. Winners: {}, Losers: {}, Total payouts: {} LLL", 
                market.getId(), winnersCount, losersCount, totalPayouts);
        
        return marketService.getMarketById(market.getId());
    }
    
    private boolean determineWinner(Order order, Market.MarketOutcome outcome) {
        if (outcome == Market.MarketOutcome.VOID) {
            return false; // No winners on void, everyone gets refunded
        }
        
        if (outcome == Market.MarketOutcome.YES) {
            return order.getSide() == Order.OrderSide.YES;
        } else {
            return order.getSide() == Order.OrderSide.NO;
        }
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
}


