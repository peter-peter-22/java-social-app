package com.example.users_persistence.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface UserCrudRepository extends CrudRepository<UserEntity, UUID> {
}
