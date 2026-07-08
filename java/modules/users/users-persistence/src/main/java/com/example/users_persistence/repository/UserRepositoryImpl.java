package com.example.users_persistence.repository;

import com.example.users.api.repository.User;
import com.example.users.api.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class UserRepositoryImpl implements UserRepository {
    private final JdbcAggregateTemplate template;

    private static @NotNull User entityToDomain(@NotNull UserEntity entity) {
        return new User(
                new UserId(entity.id())
        );
    }

    private static @NotNull UserEntity domainToEntity(@NotNull User domain) {
        return new UserEntity(
                domain.id().get()
        );
    }

    @Override
    public @Nullable User findById(@NotNull UserId id) {
        var found = template.findById(id.get(), UserEntity.class);
        if (found == null) return null;
        return entityToDomain(found);
    }

    @Override
    public @NotNull User create(@NotNull InsertUser user) {
        var e = new UserEntity(
                null
        );
        var created = template.insert(e);
        return entityToDomain(created);
    }

    @Override
    public void update(@NotNull User user) {
        var e = domainToEntity(user);
        template.update(e);
    }
}
