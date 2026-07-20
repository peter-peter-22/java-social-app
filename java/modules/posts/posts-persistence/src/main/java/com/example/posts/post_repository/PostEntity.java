package com.example.posts.post_repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("posts")
record PostEntity(
        @Id
        UUID id,
        UUID authorId,
        String body
) {
}
