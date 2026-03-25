package com.yo1000.s3import.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public NamedParameterJdbcOperations namedJdbcOperations(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public JdbcClient jdbcClient(NamedParameterJdbcOperations namedJdbcOperations) {
        return JdbcClient.create(namedJdbcOperations);
    }
}
