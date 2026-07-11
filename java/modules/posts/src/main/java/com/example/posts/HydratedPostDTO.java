package com.example.posts;

import com.example.users.api.repository.User;

public record HydratedPostDTO(
        Post post,
        User author
) {
}