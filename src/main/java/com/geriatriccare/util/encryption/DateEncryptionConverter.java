package com.geriatriccare.util.encryption;

import com.geriatriccare.service.security.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Converter
@Component
public class DateEncryptionConverter implements AttributeConverter<LocalDate, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(DateEncryptionConverter.class);
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            return encryptionService.encrypt(attribute.toString());
        } catch (Exception e) {
            logger.error("Date encryption failed: {}", e.getMessage());
            throw new RuntimeException("Date encryption failed", e);
        }
    }
    
    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            String decrypted = encryptionService.decrypt(dbData);
            return LocalDate.parse(decrypted);
        } catch (Exception e) {
            logger.error("Date decryption failed: {}", e.getMessage());
            return null;
        }
    }
}
