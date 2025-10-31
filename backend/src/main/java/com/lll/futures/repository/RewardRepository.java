package com.lll.futures.repository;

import com.lll.futures.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    List<Reward> findByUserId(Long userId);
    
    List<Reward> findByUserIdAndStatus(Long userId, Reward.RewardStatus status);
    
    List<Reward> findByStatus(Reward.RewardStatus status);
    
    Optional<Reward> findByTransactionSignature(String signature);
    
    boolean existsByTransactionSignature(String signature);
}

