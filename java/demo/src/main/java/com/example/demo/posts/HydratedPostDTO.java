package com.example.demo.posts;

import com.example.demo.users.User;

public record HydratedPostDTO(
        Post post,
        User author
) {
}