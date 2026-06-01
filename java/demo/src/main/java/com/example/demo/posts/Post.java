package com.example.demo.posts;

import com.example.demo.users.UserId;

import java.time.Instant;

public record Post (
        PostId id,
        UserId authorId,
        String text,
        Instant createdAt
) {}
