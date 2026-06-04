package com.example.demo.posts.internal.post_service;

import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class PostRepositoryCustomImpl implements PostRepositoryCustom {
    private final JdbcClient jdbc;

    public PostRepositoryCustomImpl(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public @NotNull UUID insertAndReturnId(@NotNull UUID authorId, @NotNull String body) {
        return jdbc.sql("""
                        INSERT INTO posts(author_id,body)
                        VALUES (:author_id,:body)
                        RETURNING id
                        """)
                .param("author_id", authorId)
                .param("body", body)
                .query(UUID.class)
                .single();
    }
}
