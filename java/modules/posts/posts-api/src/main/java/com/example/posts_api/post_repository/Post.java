package com.example.posts_api.post_repository;

import com.example.users_api.repository.UserId;

import java.time.Instant;

public record Post (
        PostId id,
        UserId authorId,
        String text,
        Instant createdAt
) {}
