package com.example.posts;

import com.example.users.User;

public record HydratedPostDTO(
        Post post,
        User author
) {
}