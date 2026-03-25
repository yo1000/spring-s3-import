package com.yo1000.s3import.config;

import com.yo1000.s3import.domain.vo.NodeIdHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class NodeConfig {
    @Bean
    public NodeIdHolder nodeIdHolder() {
        return new NodeIdHolder(UUID.randomUUID().toString());
    }
}
