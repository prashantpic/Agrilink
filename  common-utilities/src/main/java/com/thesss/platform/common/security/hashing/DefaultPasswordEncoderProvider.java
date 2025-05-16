package com.thesss.platform.common.security.hashing;

import com.thesss.platform.common.security.config.CommonSecurityProperties;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultPasswordEncoderProvider implements PasswordEncoderProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultPasswordEncoderProvider.class);
    private final PasswordEncoder passwordEncoder;

    public DefaultPasswordEncoderProvider(CommonSecurityProperties commonSecurityProperties) {
        String defaultEncoderId = commonSecurityProperties.getPasswordEncoderDefault();
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        // Standard encoders
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        // Argon2 with Spring Security 5.8 recommended defaults
        encoders.put("argon2id", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        // SCrypt with Spring Security 5.8 recommended defaults
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        // Pbkdf2 - If needed, configure similarly with recommended defaults
        // encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        if (!encoders.containsKey(defaultEncoderId)) {
            log.error("Default password encoder ID '{}' specified in common.security.passwordEncoderDefault is not a configured encoder. " +
                      "Available encoders: {}. Falling back to 'argon2id'.", defaultEncoderId, encoders.keySet());
            defaultEncoderId = "argon2id"; // Fallback to a secure default
            if (!encoders.containsKey(defaultEncoderId)) { // Should not happen if argon2id is always in the map
                 throw new IllegalArgumentException("Fallback default password encoder 'argon2id' is not configured.");
            }
        }
        log.info("Using '{}' as the default password encoder.", defaultEncoderId);
        this.passwordEncoder = new DelegatingPasswordEncoder(defaultEncoderId, encoders);
    }

    @Override
    public PasswordEncoder getEncoder() {
        return this.passwordEncoder;
    }
}