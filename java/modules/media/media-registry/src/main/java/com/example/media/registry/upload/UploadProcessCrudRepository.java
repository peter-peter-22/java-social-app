package com.example.media.registry.upload;

import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

@NullMarked
interface UploadProcessCrudRepository extends JpaRepository<UploadProcessEntity, String> {
    @EntityGraph(attributePaths = {"activeAttempt", "upload"})
    Optional<UploadProcessEntity> findWithReferencesByUploadKey(String uploadKey);

    @Modifying
    @Query(value = """
            INSERT INTO upload_process (upload_key)
            VALUES (:uploadKey)
            ON CONFLICT (upload_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfNotExists(String uploadKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"activeAttempt", "upload"})
    @Query("SELECT p FROM UploadProcessEntity p WHERE p.uploadKey = :uploadKey")
    Optional<UploadProcessEntity> findByUploadKeyForUpdate(String uploadKey);
}
