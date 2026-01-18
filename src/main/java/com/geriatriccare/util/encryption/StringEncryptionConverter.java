package com.geriatriccare.util.encryption;

import com.geriatriccare.service.security.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class StringEncryptionConverter implements AttributeConverter<String, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(StringEncryptionConverter.class);
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            return encryptionService.encrypt(attribute);
        } catch (Exception e) {
            logger.error("Encryption failed during database write: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            return encryptionService.decrypt(dbData);
        } catch (Exception e) {
            logger.error("Decryption failed during database read: {}", e.getMessage());
            return dbData;
        }
    }
}
