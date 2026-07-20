package com.example.posts_api.post_repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostRepository {
    @NotNull PostId insertAndReturnId(@NotNull PostToInsert insert);
    @Nullable Post getPost(@NotNull PostId id);
}
