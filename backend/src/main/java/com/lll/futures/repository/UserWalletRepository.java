package com.lll.futures.repository;

import com.lll.futures.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
    
    Optional<UserWallet> findByUserId(Long userId);
    
    Optional<UserWallet> findByPublicKey(String publicKey);
    
    boolean existsByUserId(Long userId);
    
    boolean existsByPublicKey(String publicKey);
}

