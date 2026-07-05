package com.example.posts;

import com.example.users.persistence.repository.User;

public record HydratedPostDTO(
        Post post,
        User author
) {
}