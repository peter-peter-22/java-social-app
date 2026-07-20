package com.example.posts.post_repository;

import com.example.posts_api.post_repository.Post;
import com.example.posts_api.post_repository.PostId;
import com.example.posts_api.post_repository.PostRepository;
import com.example.posts_api.post_repository.PostToInsert;
import com.example.users_api.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class PostRepositoryImpl implements PostRepository {
    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbc;

    private static @NotNull Post entityToDomain(@NotNull PostEntity e) {
        return new Post(
                new PostId(e.id()),
                new UserId(e.authorId()),
                e.body(),
                Instant.now()
        );
    }

    @Override
    public @NotNull PostId insertAndReturnId(@NotNull PostToInsert insert) {
        var inserted = jdbc.sql("""
                        INSERT INTO posts (author_id,body)
                        VALUES (:author_id,:body)
                        RETURNING id
                        """)
                .param("author_id", insert.authorId())
                .param("body", insert.text())
                .query(UUID.class)
                .single();
        return new PostId(inserted);
    }

    @Override
    public @Nullable Post getPost(@NotNull PostId id) {
        var found = template.findById(id.get(), PostEntity.class);
        if (found == null) return null;
        return entityToDomain(found);
    }
}
