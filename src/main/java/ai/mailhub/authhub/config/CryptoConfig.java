package ai.mailhub.authhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


@Configuration
public class CryptoConfig {

    @Bean
    SecretKey principalIdSecret(){
        // DEV ONLY — TODO replace with Vault

        byte[] keyBytes = "dev-only-secret-change-me"
                .getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
