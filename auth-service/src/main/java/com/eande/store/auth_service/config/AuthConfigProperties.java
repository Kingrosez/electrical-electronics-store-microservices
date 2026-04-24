package com.eande.store.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperties {
    private int maxFailedAttempts = 5;
    private int accountLockoutDurationMinutes = 15;

    // Getters and Setters
    public int getMaxFailedAttempts() { return maxFailedAttempts; }
    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public int getAccountLockoutDurationMinutes() { return accountLockoutDurationMinutes; }
    public void setAccountLockoutDurationMinutes(int accountLockoutDurationMinutes) {
        this.accountLockoutDurationMinutes = accountLockoutDurationMinutes;
    }
}
