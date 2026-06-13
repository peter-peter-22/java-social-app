package com.example.posts;

import com.example.users.repository.UserId;

import java.time.Instant;

public record Post (
        PostId id,
        UserId authorId,
        String text,
        Instant createdAt
) {}
