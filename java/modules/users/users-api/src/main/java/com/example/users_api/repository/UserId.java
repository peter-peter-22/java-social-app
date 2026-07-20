package com.example.users_api.repository;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record UserId(@NonNull UUID get) {
}
