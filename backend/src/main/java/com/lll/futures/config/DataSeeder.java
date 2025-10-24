package com.lll.futures.config;

import com.lll.futures.model.Market;
import com.lll.futures.model.User;
import com.lll.futures.repository.MarketRepository;
import com.lll.futures.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    
    @Override
    public void run(String... args) {
        log.info("Starting data seeding...");
        
        // Only seed if database is empty
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping seeding");
            return;
        }
        
        log.info("Database is empty, seeding initial data...");
        
        // Create users
        User admin = createUser("admin", "admin@lllfutures.com", 10000.0, true);
        User alice = createUser("alice", "alice@example.com", 1000.0, false);
        User bob = createUser("bob", "bob@example.com", 1000.0, false);
        User charlie = createUser("charlie", "charlie@example.com", 1000.0, false);
        
        log.info("Created {} users", userRepository.count());
        
        // Create sample markets
        createMarket(
                "Will Bitcoin reach $100K by December 2025?",
                "This market resolves YES if Bitcoin (BTC) trades at or above $100,000 on any major exchange before December 31, 2025 23:59 UTC.",
                LocalDateTime.now().plusMonths(2),
                2.5,
                1.5,
                admin
        );
        
        createMarket(
                "Will Ethereum pass $5K this year?",
                "This market resolves YES if Ethereum (ETH) reaches $5,000 or higher on any major exchange before December 31, 2025.",
                LocalDateTime.now().plusMonths(2),
                3.0,
                1.4,
                admin
        );
        
        createMarket(
                "Will Apple announce AR glasses in 2025?",
                "This market resolves YES if Apple officially announces a consumer AR/VR glasses product during 2025.",
                LocalDateTime.now().plusMonths(4),
                4.0,
                1.3,
                alice
        );
        
        createMarket(
                "Will AI surpass human coding ability by end of 2025?",
                "This market resolves YES if a major AI benchmarking organization declares that AI has surpassed average human programmer capability by December 31, 2025.",
                LocalDateTime.now().plusMonths(2),
                2.0,
                2.0,
                bob
        );
        
        createMarket(
                "Will SpaceX land humans on Mars by 2026?",
                "This market resolves YES if SpaceX successfully lands at least one human on Mars before January 1, 2027.",
                LocalDateTime.now().plusMonths(6),
                10.0,
                1.1,
                admin
        );
        
        createMarket(
                "Will remote work become mandatory in tech?",
                "This market resolves YES if more than 50% of major tech companies (FAANG+) mandate remote-first policies by end of 2025.",
                LocalDateTime.now().plusMonths(3),
                3.5,
                1.35,
                charlie
        );
        
        log.info("Created {} markets", marketRepository.count());
        log.info("Data seeding completed successfully!");
    }
    
    private User createUser(String username, String email, Double balance, Boolean isAdmin) {
        User user = User.builder()
                .username(username)
                .email(email)
                .tokenBalance(balance)
                .isAdmin(isAdmin)
                .build();
        return userRepository.save(user);
    }
    
    private Market createMarket(String title, String description, LocalDateTime expiryDate,
                               Double yesOdds, Double noOdds, User creator) {
        Market market = Market.builder()
                .title(title)
                .description(description)
                .expiryDate(expiryDate)
                .yesOdds(yesOdds)
                .noOdds(noOdds)
                .creator(creator)
                .status(Market.MarketStatus.ACTIVE)
                .totalYesStake(0.0)
                .totalNoStake(0.0)
                .totalVolume(0.0)
                .build();
        return marketRepository.save(market);
    }
}


