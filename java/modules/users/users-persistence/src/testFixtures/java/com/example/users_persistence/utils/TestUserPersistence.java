package com.example.users_persistence.utils;

import com.example.users_api.repository.InsertUser;
import com.example.users_api.repository.User;
import com.example.users_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class TestUserPersistence {
    private final UserRepository userRepository;

    public static InsertUser createUserInsert(@Nullable Consumer<InsertUser.@Nullable InsertUserBuilder> customizer) {
        var builder = InsertUser.builder();

        if (customizer != null)
            customizer.accept(builder);

        return builder.build();
    }

    public User insertUser(@Nullable Consumer<InsertUser.@Nullable InsertUserBuilder> customizer) {
        var insert = createUserInsert(customizer);
        return userRepository.create(insert);
    }

    public User insertUser() {
        return insertUser(null);
    }

}
