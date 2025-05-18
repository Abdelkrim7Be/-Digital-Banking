package com.bellagnech.dig_bank.security.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage blacklisted JWT tokens 
 * This is a simple in-memory implementation. In production, you might want to use Redis or a database.
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Add a token to the blacklist
     */
    public void blacklistToken(String token) {
        log.info("Adding token to blacklist");
        blacklistedTokens.add(token);
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Remove a token from the blacklist
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }

    /**
     * Clear all blacklisted tokens
     * Mainly for testing purposes
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }
}
