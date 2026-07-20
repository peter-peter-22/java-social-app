package com.example.posts_api.post_repository;

import com.example.users_api.repository.UserId;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public record PostToInsert(
        @NotNull UserId authorId,
        @NotNull String text
) {
}
