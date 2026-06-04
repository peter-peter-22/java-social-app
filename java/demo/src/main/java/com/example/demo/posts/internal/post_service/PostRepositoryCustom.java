package com.example.demo.posts.internal.post_service;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

interface PostRepositoryCustom {
    @NotNull UUID insertAndReturnId(@NotNull UUID authorId, @NotNull String body);
}
