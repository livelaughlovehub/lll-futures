package com.lll.futures.service;

import com.lll.futures.model.UserWallet;
import com.lll.futures.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final UserWalletRepository userWalletRepository;
    
    @Value("${app.wallet.encryption-key:lll-futures-wallet-key-change-in-production}")
    private String encryptionKey;
    
    // Initialize BouncyCastle provider for Ed25519
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    
    /**
     * Create a new wallet for a user
     * Generates a real Solana keypair
     */
    @Transactional
    public UserWallet createUserWallet(Long userId) {
        try {
            // Check if wallet already exists
            Optional<UserWallet> existing = userWalletRepository.findByUserId(userId);
            if (existing.isPresent()) {
                log.warn("User {} already has a wallet", userId);
                return existing.get();
            }
            
            // Generate a complete Solana keypair
            KeyPairData keyPairData = generateSolanaKeypair();
            
            // Encrypt the private key
            String encryptedPrivateKey = encryptPrivateKey(keyPairData.privateKeyBase64);
            
            // Save to database
            UserWallet wallet = UserWallet.builder()
                .userId(userId)
                .publicKey(keyPairData.publicKeyBase58)
                .encryptedPrivateKey(encryptedPrivateKey)
                .build();
            
            wallet = userWalletRepository.save(wallet);
            
            log.info("Created wallet for user {} with public key: {}", userId, keyPairData.publicKeyBase58);
            return wallet;
            
        } catch (Exception e) {
            log.error("Error creating wallet for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to create wallet", e);
        }
    }
    
    /**
     * Internal class to hold keypair data
     */
    private static class KeyPairData {
        String publicKeyBase58;
        String privateKeyBase64;
        
        KeyPairData(String publicKeyBase58, String privateKeyBase64) {
            this.publicKeyBase58 = publicKeyBase58;
            this.privateKeyBase64 = privateKeyBase64;
        }
    }
    
    /**
     * Generate a real Solana keypair using Ed25519
     * Returns both public key (Base58) and private key (Base64 encoded keypair)
     */
    private KeyPairData generateSolanaKeypair() {
        try {
            // Generate Ed25519 keypair
            SecureRandom secureRandom = new SecureRandom();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", "BC");
            keyPairGenerator.initialize(256, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // Get public key bytes
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            
            // Extract raw public key (last 32 bytes of encoded format for Ed25519)
            byte[] rawPublicKey;
            if (publicKeyBytes.length >= 32) {
                rawPublicKey = new byte[32];
                System.arraycopy(publicKeyBytes, publicKeyBytes.length - 32, rawPublicKey, 0, 32);
            } else {
                rawPublicKey = publicKeyBytes;
            }
            
            // Get private key bytes
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            
            // Extract raw private key material
            byte[] rawPrivateKey;
            if (privateKeyBytes.length >= 32) {
                rawPrivateKey = new byte[32];
                System.arraycopy(privateKeyBytes, privateKeyBytes.length - 32, rawPrivateKey, 0, 32);
            } else {
                rawPrivateKey = privateKeyBytes;
            }
            
            // Solana uses 64-byte keypair format: private key (32 bytes) + public key (32 bytes)
            byte[] solanaKeypair = new byte[64];
            System.arraycopy(rawPrivateKey, 0, solanaKeypair, 0, 32);
            System.arraycopy(rawPublicKey, 0, solanaKeypair, 32, 32);
            
            // Encode public key to Base58 (Solana address format)
            String publicKeyBase58 = Base58.encode(rawPublicKey);
            
            // Encode private keypair to Base64 for storage
            String privateKeyBase64 = Base64.getEncoder().encodeToString(solanaKeypair);
            
            log.debug("Generated Solana keypair - Public: {} (length: {})", publicKeyBase58, publicKeyBase58.length());
            
            return new KeyPairData(publicKeyBase58, privateKeyBase64);
            
        } catch (Exception e) {
            log.error("Error generating Solana keypair: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Solana keypair", e);
        }
    }
    
    /**
     * Encrypt private key for secure storage
     */
    private String encryptPrivateKey(String privateKey) {
        try {
            // Create AES cipher
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encrypted = cipher.doFinal(privateKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            log.error("Error encrypting private key: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt private key", e);
        }
    }
    
    /**
     * Decrypt private key from database
     */
    public String decryptPrivateKey(String encryptedPrivateKey) {
        try {
            // Create AES cipher
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPrivateKey));
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error decrypting private key: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt private key", e);
        }
    }
    
    /**
     * Get user's wallet
     */
    public Optional<UserWallet> getUserWallet(Long userId) {
        return userWalletRepository.findByUserId(userId);
    }
    
    /**
     * Get wallet by public key
     */
    public Optional<UserWallet> getWalletByPublicKey(String publicKey) {
        return userWalletRepository.findByPublicKey(publicKey);
    }
}

/**
 * Simple Base58 encoder for Solana addresses
 */
class Base58 {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    
    public static String encode(byte[] input) {
        if (input.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        long value = 0;
        for (byte b : input) {
            value = (value * 256) + (b & 0xFF);
        }
        
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % 58)));
            value /= 58;
        }
        
        // Handle leading zeros
        for (byte b : input) {
            if (b == 0) {
                sb.append(ALPHABET.charAt(0));
            } else {
                break;
            }
        }
        
        return sb.reverse().toString();
    }
}

