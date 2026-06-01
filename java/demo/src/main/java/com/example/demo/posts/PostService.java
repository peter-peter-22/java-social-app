package com.example.demo.posts;

import com.example.demo.users.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostService {
    @NotNull Post createPost(@NotNull UserId authorId, @NotNull String text);
    @Nullable Post getPost(@NotNull PostId id);
}
