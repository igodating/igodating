package com.bpcbt.marketplace.boot.chat.config;

import com.bpcbt.marketplace.basic.db.postgres.QueryBuilderConfiguration;
import com.bpcbt.marketplace.chat.db.api.ChatRepository;
import com.bpcbt.marketplace.chat.db.postgres.ChatPostgresRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pe.kwonnam.freemarkerdynamicqlbuilder.FreemarkerDynamicQlBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@Log4j2
public class JdbcConfig {

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource primaryDataSource) {
        return new NamedParameterJdbcTemplate(primaryDataSource);
    }

    @Bean
    public FreemarkerDynamicQlBuilder freemarkerDynamicQlBuilder() {
        return QueryBuilderConfiguration.createFreemarkerDynamicQlBuilder();
    }

    @Bean
    public ChatRepository chatRepository(NamedParameterJdbcTemplate jdbcTemplate, FreemarkerDynamicQlBuilder freemarkerDynamicQlBuilder, ObjectMapper objectMapper) {
        return new ChatPostgresRepository(jdbcTemplate, objectMapper, freemarkerDynamicQlBuilder);
    }
}
