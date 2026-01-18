package com.geriatriccare.dto.security;

public enum MFAMethod {
    TOTP("Authenticator App", "Time-based One-Time Password (Google Authenticator, Authy)"),
    SMS("SMS", "Text message to registered phone number"),
    EMAIL("Email", "Verification code sent to email"),
    BACKUP_CODES("Backup Codes", "Pre-generated backup codes");
    
    private final String displayName;
    private final String description;
    
    MFAMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
