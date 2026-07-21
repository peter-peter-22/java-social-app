package com.example.users_api.repository;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface UserRepository {
    @Nullable User findById(@NonNull UserId id);

    @NonNull User create(@NonNull InsertUser user);

    void update(@NonNull User user);
}
