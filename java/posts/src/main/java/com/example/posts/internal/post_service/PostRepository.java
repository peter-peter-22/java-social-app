package com.example.posts.internal.post_service;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface PostRepository extends CrudRepository<PostEntity, UUID>, PostRepositoryCustom {
}