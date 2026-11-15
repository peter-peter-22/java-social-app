package com.example.upload_registry.upload_process;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@NullMarked
interface UploadAttemptCrudRepository extends JpaRepository<UploadAttemptEntity, UUID> {
    @EntityGraph(attributePaths = {"uploadProcess", "originalAsset"})
    Optional<UploadAttemptEntity> findWithReferencesByAttemptId(UUID attemptId);
}
