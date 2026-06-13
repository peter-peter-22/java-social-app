package com.example.posts;

import com.example.users.repository.User;

public record HydratedPostDTO(
        Post post,
        User author
) {
}