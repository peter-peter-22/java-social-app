package com.example.users.repository;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UserId(@NotNull UUID get) {
}
