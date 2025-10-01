package com.geriatriccare.base;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseIntegrationVerificationTest extends BaseIntegrationTest {
    
    @Test
    void testSetupWorks() {
        assertThat(ownerToken).isNotNull();
    }
}
