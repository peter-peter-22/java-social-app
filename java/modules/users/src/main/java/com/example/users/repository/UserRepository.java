package com.example.users.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserRepository {
    @Nullable User findById(UserId id);
    @NotNull User create(InsertUser user);
    void update(User user);
}
