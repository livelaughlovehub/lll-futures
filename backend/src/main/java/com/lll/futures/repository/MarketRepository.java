package com.lll.futures.repository;

import com.lll.futures.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByStatus(Market.MarketStatus status);
    List<Market> findByCreatorId(Long creatorId);
}


