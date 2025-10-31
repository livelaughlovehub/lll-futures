package com.lll.futures.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

@Service
@Slf4j
public class VaultService {
    
    // Initialize BouncyCastle provider for Ed25519
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    
    @Value("${app.vault.public-key}")
    private String vaultPublicKey;
    
    @Value("${app.vault.encrypted-private-key:}")
    private String encryptedPrivateKey;
    
    @Value("${app.wallet.encryption-key}")
    private String encryptionKey;
    
    // Vault private key as byte array (Solana uses 64-byte keypairs)
    private byte[] vaultPrivateKeyBytes;
    
    /**
     * Initialize vault on application startup
     */
    @PostConstruct
    public void init() {
        initializePrivateKey();
        initializeEd25519Keys();
        log.info("Vault service initialized - Public Key: {}", vaultPublicKey);
    }
    
    /**
     * Initialize vault private key
     * Called during application startup or when vault key is updated
     */
    public void initializePrivateKey() {
        // Initialize vault private key from the provided byte array
        // This is the private key you provided
        vaultPrivateKeyBytes = new byte[]{
            (byte)184,(byte)188,(byte)16,(byte)138,(byte)43,(byte)127,(byte)80,(byte)93,(byte)66,(byte)138,(byte)250,(byte)35,(byte)12,(byte)153,(byte)81,(byte)161,
            (byte)200,(byte)8,(byte)240,(byte)18,(byte)111,(byte)22,(byte)238,(byte)208,(byte)50,(byte)246,(byte)169,(byte)98,(byte)86,(byte)178,(byte)4,(byte)125,
            (byte)15,(byte)123,(byte)41,(byte)127,(byte)110,(byte)222,(byte)100,(byte)193,(byte)162,(byte)161,(byte)171,(byte)176,(byte)137,(byte)83,(byte)129,
            (byte)140,(byte)58,(byte)21,(byte)77,(byte)174,(byte)19,(byte)55,(byte)176,(byte)35,(byte)160,(byte)154,(byte)176,(byte)148,(byte)32,(byte)155,(byte)22,(byte)99
        };
        
        log.info("Vault private key initialized successfully");
    }
    
    /**
     * Get the vault private key
     * @return Vault private key as byte array
     */
    public byte[] getVaultPrivateKey() {
        if (vaultPrivateKeyBytes == null) {
            initializePrivateKey();
        }
        return vaultPrivateKeyBytes;
    }
    
    /**
     * Get the vault public key (this is where rewards come from)
     */
    public String getVaultPublicKey() {
        return vaultPublicKey;
    }
    
    /**
     * Check if vault is configured
     */
    public boolean isConfigured() {
        return vaultPublicKey != null && !vaultPublicKey.isEmpty();
    }
    
    /**
     * Get vault balance
     * This queries the Solana blockchain for the vault's token balance
     */
    public Double getVaultBalance(SolanaService solanaService) {
        if (!isConfigured()) {
            log.warn("Vault not configured");
            return 0.0;
        }
        
        try {
            return solanaService.getTokenBalance(vaultPublicKey);
        } catch (Exception e) {
            log.error("Error getting vault balance: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Log vault status
     */
    public void logVaultStatus(SolanaService solanaService) {
        if (isConfigured()) {
            Double balance = getVaultBalance(solanaService);
            log.info("Vault Status - Public Key: {}, Balance: {} LLL", vaultPublicKey, balance);
        } else {
            log.warn("Vault not configured properly");
        }
    }
    
    /**
     * Initialize Ed25519 keys from the vault keypair
     * Just validates the keypair is properly initialized
     */
    private void initializeEd25519Keys() {
        if (vaultPrivateKeyBytes == null || vaultPrivateKeyBytes.length != 64) {
            log.error("Vault keypair is not properly initialized (expected 64 bytes, got {})", 
                vaultPrivateKeyBytes != null ? vaultPrivateKeyBytes.length : 0);
        } else {
            log.info("Vault keypair bytes initialized (64 bytes) - ready for signing");
        }
    }
    
    /**
     * Sign a transaction with the vault's Ed25519 private key
     * @param data The transaction data to sign
     * @return The signature bytes
     */
    public byte[] signTransaction(byte[] data) {
        try {
            if (vaultPrivateKeyBytes == null || vaultPrivateKeyBytes.length != 64) {
                log.error("Vault keypair not initialized");
                throw new RuntimeException("Vault signing not initialized");
            }
            
            // Extract private key bytes (first 32 bytes of the 64-byte keypair)
            byte[] privateKeyBytes = Arrays.copyOfRange(vaultPrivateKeyBytes, 0, 32);
            
            // Create Ed25519 private key spec
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519", "BC");
            EdECPrivateKeySpec privateKeySpec = new EdECPrivateKeySpec(
                NamedParameterSpec.ED25519, privateKeyBytes
            );
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            
            // Sign the transaction
            Signature signature = Signature.getInstance("Ed25519", "BC");
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            
            log.debug("Signed transaction with Ed25519 (signature length: {})", signatureBytes.length);
            return signatureBytes;
            
        } catch (Exception e) {
            log.error("Failed to sign transaction with vault key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sign transaction", e);
        }
    }
    
    /**
     * Get the raw vault keypair bytes
     */
    public byte[] getVaultKeypair() {
        return vaultPrivateKeyBytes;
    }
}

