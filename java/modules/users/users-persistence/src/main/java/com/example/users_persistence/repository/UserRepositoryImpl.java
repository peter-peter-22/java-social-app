package com.example.users_persistence.repository;

import com.example.users_api.repository.User;
import com.example.users_api.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class UserRepositoryImpl implements UserRepository {
    private final JdbcAggregateTemplate template;

    private static @NonNull User entityToDomain(@NonNull UserEntity entity) {
        return new User(
                new UserId(entity.id())
        );
    }

    private static @NonNull UserEntity domainToEntity(@NonNull User domain) {
        return new UserEntity(
                domain.id().get()
        );
    }

    @Override
    public @Nullable User findById(@NonNull UserId id) {
        var found = template.findById(id.get(), UserEntity.class);
        if (found == null) return null;
        return entityToDomain(found);
    }

    @Override
    public @NonNull User create(@NonNull InsertUser user) {
        var e = new UserEntity(
                null
        );
        var created = template.insert(e);
        return entityToDomain(created);
    }

    @Override
    public void update(@NonNull User user) {
        var e = domainToEntity(user);
        template.update(e);
    }
}
