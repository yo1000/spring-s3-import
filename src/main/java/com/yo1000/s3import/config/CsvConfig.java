package com.yo1000.s3import.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvConfig {
    @Bean
    public CsvSchema csvSchema() {
        return CsvSchema.builder()
                .addColumn("username", CsvSchema.ColumnType.STRING)
                .addColumn("email", CsvSchema.ColumnType.STRING)
                .addColumn("givenName", CsvSchema.ColumnType.STRING)
                .addColumn("familyName", CsvSchema.ColumnType.STRING)
                .addColumn("gender", CsvSchema.ColumnType.STRING)
                .addColumn("birthDate", CsvSchema.ColumnType.STRING)
                .addColumn("address", CsvSchema.ColumnType.STRING)
                .build()
                .withHeader()
                .withColumnSeparator(',');
    }

    @Bean
    public ObjectMapper csvMapper() {
        ObjectMapper csvMapper = new CsvMapper();
        return csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
