package com.lll.futures.repository;

import com.lll.futures.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByWalletAddress(String walletAddress);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByWalletAddress(String walletAddress);
}


