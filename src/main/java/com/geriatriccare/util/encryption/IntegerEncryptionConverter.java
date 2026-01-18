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
public class IntegerEncryptionConverter implements AttributeConverter<Integer, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegerEncryptionConverter.class);
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(Integer attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            return encryptionService.encrypt(attribute.toString());
        } catch (Exception e) {
            logger.error("Integer encryption failed: {}", e.getMessage());
            throw new RuntimeException("Integer encryption failed", e);
        }
    }
    
    @Override
    public Integer convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            String decrypted = encryptionService.decrypt(dbData);
            return Integer.parseInt(decrypted);
        } catch (Exception e) {
            logger.error("Integer decryption failed: {}", e.getMessage());
            return null;
        }
    }
}
