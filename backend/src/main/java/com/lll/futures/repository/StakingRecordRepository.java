package com.lll.futures.repository;

import com.lll.futures.model.StakingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StakingRecordRepository extends JpaRepository<StakingRecord, Long> {
    List<StakingRecord> findByWalletAddress(String walletAddress);
    List<StakingRecord> findByWalletAddressAndAction(String walletAddress, StakingRecord.StakingAction action);
}
