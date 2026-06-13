package com.example.users.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface UserCrudRepository extends CrudRepository<UserEntity, UUID> {
}
