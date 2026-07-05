package com.example.users.persistence.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users")
record UserEntity(
        @Id UUID id
) {
}
