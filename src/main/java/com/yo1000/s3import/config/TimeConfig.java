package com.yo1000.s3import.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
