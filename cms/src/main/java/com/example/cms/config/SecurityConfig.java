package com.example.cms.config; // Adjust the package name to where you are placing this file

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class SecurityConfig {
    // Your configurations, if any
}
