package com.example.cockroach_db;

import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

public class ForeignKeyErrorReader {
    public static boolean isForeignKeyError(@NonNull DataIntegrityViolationException e, @NonNull String foreignKeyConstraintName) {
        if (e.getCause() instanceof SQLException cause) {
            return cause.getSQLState().equals(SQLErrorCodes.FOREIGN_KEY_VIOLATION) && e.getMessage().contains(foreignKeyConstraintName);
        }
        return false;
    }
}
