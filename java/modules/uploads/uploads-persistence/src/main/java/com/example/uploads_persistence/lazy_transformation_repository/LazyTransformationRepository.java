package com.example.uploads_persistence.lazy_transformation_repository;

import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_api.uploads.UploadStatus;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class LazyTransformationRepository {
    private final JdbcClient jdbc;
    private final JdbcTemplate template;

    /**
     * Marks a pending lazy transformation as ready and deletes the session if no more lazy transformations are queued.
     */
    public void markLazyTransformationAsComplete(@NotNull UploadId uploadId, @NotNull String transformationName) {
        // CLEAN should be simplified?
        jdbc.sql("""
                        WITH deleted_pending AS (
                            DELETE FROM pending_lazy_transformations
                            WHERE session_id = :session_id
                              AND transformation_name = :transformation_name
                            RETURNING session_id
                        ),
                        deleted_session AS (
                            DELETE FROM lazy_transformation_sessions
                            WHERE upload_id IN (SELECT session_id FROM deleted_pending)
                              AND NOT EXISTS (
                                  SELECT 1
                                  FROM pending_lazy_transformations
                                  WHERE session_id IN (SELECT session_id FROM deleted_pending)
                                    AND transformation_name <> :transformation_name
                              )
                            RETURNING upload_id
                        )
                        UPDATE uploads
                        SET status = :status
                        WHERE id IN (SELECT upload_id FROM deleted_session)
                        """)
                .param("session_id", uploadId.get())
                .param("transformation_name", transformationName)
                .param("status", UploadStatus.READY.name())
                .update();
    }

    /**
     * Creates a new lazy transformation session for the given upload and awaited lazy transformations.
     */
    @Transactional
    public void createLazyTransformationSession(@NotNull UploadId uploadId, @NotNull Collection<@NotNull String> requiredTransformations) {

        // insert session
        template.update("INSERT INTO lazy_transformation_sessions (upload_id) VALUES (?)", uploadId.get());

        // insert pending transformations
        template.batchUpdate(
                "INSERT INTO pending_lazy_transformations (session_id, transformation_name) VALUES (?, ?)",
                requiredTransformations,
                requiredTransformations.size(),
                (ps,name)->{
                    ps.setObject(1, uploadId.get());
                    ps.setObject(2, name);
                }
        );
    }
}
