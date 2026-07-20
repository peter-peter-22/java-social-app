package com.example.posts_api.post_repository;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface PostRepository {
    @NonNull PostId insertAndReturnId(@NonNull PostToInsert insert);

    @Nullable Post getPost(@NonNull PostId id);
}
