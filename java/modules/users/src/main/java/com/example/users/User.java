package com.example.users;

import org.springframework.data.annotation.Id;

public record User (
    @Id UserId id
) {}
