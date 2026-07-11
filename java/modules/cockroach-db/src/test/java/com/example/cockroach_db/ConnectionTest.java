package com.example.cockroach_db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConnectionTest extends CockroachIntegrationTest {
    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void testConnection() {
        Integer result = jdbcClient.sql("select 1 as result").query(Integer.class).single();
        assertThat(result).isEqualTo(1);
    }
}
