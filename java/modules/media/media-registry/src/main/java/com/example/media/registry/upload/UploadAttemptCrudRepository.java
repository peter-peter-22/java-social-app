package com.example.media.registry.upload;

import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
interface UploadAttemptCrudRepository extends JpaRepository<UploadAttemptEntity, UUID> {
    @EntityGraph(attributePaths = {"uploadProcess", "originalAsset"})
    Optional<UploadAttemptEntity> findWithReferencesByAttemptId(UUID attemptId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM UploadAttemptEntity a WHERE a.attemptId = :attemptId")
    Optional<UploadAttemptEntity> findByAttemptIdForUpdate(UUID attemptId);
}
