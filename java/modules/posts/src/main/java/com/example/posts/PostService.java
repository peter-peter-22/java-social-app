package com.example.posts;

import com.example.users.api.repository.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostService {
    @NotNull PostId insertAndReturnId(@NotNull UserId authorId, @NotNull String text);
    @Nullable Post getPost(@NotNull PostId id);
}
