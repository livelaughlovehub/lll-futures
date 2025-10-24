package com.lll.futures.repository;

import com.lll.futures.model.TradingReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradingRewardRepository extends JpaRepository<TradingReward, Long> {
    List<TradingReward> findByWalletAddress(String walletAddress);
    List<TradingReward> findByWalletAddressAndRewardType(String walletAddress, TradingReward.RewardType rewardType);
    
    @Query("SELECT tr FROM TradingReward tr WHERE tr.walletAddress = :walletAddress AND tr.rewardType = :rewardType AND DATE(tr.createdAt) = DATE(:date)")
    List<TradingReward> findByWalletAddressAndRewardTypeAndDate(@Param("walletAddress") String walletAddress, 
                                                               @Param("rewardType") TradingReward.RewardType rewardType,
                                                               @Param("date") LocalDateTime date);
}
