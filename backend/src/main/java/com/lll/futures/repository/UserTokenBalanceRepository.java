package com.lll.futures.repository;

import com.lll.futures.model.UserTokenBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenBalanceRepository extends JpaRepository<UserTokenBalance, Long> {
    Optional<UserTokenBalance> findByWalletAddress(String walletAddress);
    boolean existsByWalletAddress(String walletAddress);
}
