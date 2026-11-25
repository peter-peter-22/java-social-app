package com.example.media.registry.upload;

import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
interface UploadPermissionCrudRepository extends JpaRepository<UploadPermissionEntity, UUID> {

    Optional<UploadPermissionEntity> findByTokenHash(String tokenHash);

    /** Locks the row for the rest of the caller's transaction; a concurrent consume waits here. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UploadPermissionEntity p WHERE p.tokenHash = :tokenHash")
    Optional<UploadPermissionEntity> findByTokenHashForUpdate(String tokenHash);
}
