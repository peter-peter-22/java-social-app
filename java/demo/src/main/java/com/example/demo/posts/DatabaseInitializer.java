package com.example.demo.posts;

import com.example.demo.posts.internal.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate=jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            jdbcTemplate.execute("SELECT * FROM posts");
            System.out.println("[DEBUG_LOG] Table posts exists");
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Table posts error: " + e.getMessage());
        }
    }
}
