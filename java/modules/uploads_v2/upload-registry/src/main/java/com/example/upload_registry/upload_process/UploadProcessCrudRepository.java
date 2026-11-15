package com.example.upload_registry.upload_process;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@NullMarked
interface UploadProcessCrudRepository extends JpaRepository<UploadProcessEntity, String> {
    @EntityGraph(attributePaths = {"activeAttempt", "upload"})
    Optional<UploadProcessEntity> findWithReferencesByUploadKey(String uploadKey);
}
