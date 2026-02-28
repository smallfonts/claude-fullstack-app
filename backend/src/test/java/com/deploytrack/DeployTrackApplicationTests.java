package com.deploytrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "udeploy.base-url=http://localhost:9999",
    "udeploy.auth-token=test-token",
    "udeploy.ssl-verify=false"
})
class DeployTrackApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context starts successfully
    }
}
