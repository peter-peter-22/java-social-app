package com.example.users_persistence.repository;

import com.example.users_api.repository.User;
import com.example.users_api.repository.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserRepository {
    @Nullable User findById(@NotNull UserId id);
    @NotNull User create(@NotNull InsertUser user);
    void update(@NotNull User user);
}
