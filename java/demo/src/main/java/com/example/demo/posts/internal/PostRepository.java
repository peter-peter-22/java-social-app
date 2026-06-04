package com.example.demo.posts.internal;

import com.example.demo.posts.PostId;
import com.example.demo.users.UserId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository extends CrudRepository<PostEntity, PostId> {
    // We got to add id-less insert here
    @Query("INSERT INTO posts (author_id, body) VALUES (:author_id, :body) RETURNING id")
    PostId create(@Param("author_id") @NotNull UUID authorId, @NotNull @Param("body") String body);
}