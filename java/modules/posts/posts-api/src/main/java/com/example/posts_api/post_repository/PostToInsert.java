package com.example.posts_api.post_repository;

import com.example.users_api.repository.UserId;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record PostToInsert(
        @NonNull UserId authorId,
        @NonNull String text
) {
}
