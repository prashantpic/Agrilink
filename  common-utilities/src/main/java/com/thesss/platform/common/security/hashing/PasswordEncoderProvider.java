package com.thesss.platform.common.security.hashing;

import org.springframework.security.crypto.password.PasswordEncoder;

public interface PasswordEncoderProvider {

    PasswordEncoder getEncoder();
}