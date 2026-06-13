package com.example.users.repository;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class UserRepositoryImpl implements UserRepository {
    private final UserCrudRepository userCrudRepository;
    private final JdbcAggregateTemplate template;

    private static User entityToDomain(UserEntity entity) {
        return new User(
                new UserId(entity.id())
        );
    }

    private static UserEntity domainToEntity(User domain) {
        return new UserEntity(
                domain.id().get()
        );
    }

    @Override
    public @Nullable User findById(UserId id) {
        return userCrudRepository.findById(id.get())
                .map(UserRepositoryImpl::entityToDomain)
                .orElse(null);
    }

    @Override
    public @NotNull User create(InsertUser user) {
        var e = new UserEntity(
                null
        );
        var created = template.insert(e);
        return entityToDomain(created);
    }

    @Override
    public void update(User user) {
        var e = domainToEntity(user);
        template.update(e);
    }
}
