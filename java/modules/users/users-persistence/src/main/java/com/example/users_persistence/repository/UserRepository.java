package com.example.users_persistence.repository;

import com.example.users_api.repository.User;
import com.example.users_api.repository.UserId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface UserRepository {
    @Nullable User findById(@NonNull UserId id);

    @NonNull User create(@NonNull InsertUser user);

    void update(@NonNull User user);
}
