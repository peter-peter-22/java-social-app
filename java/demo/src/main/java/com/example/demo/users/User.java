package com.example.demo.users;

import org.springframework.data.annotation.Id;

public record User (
    @Id UserId id
) {}
