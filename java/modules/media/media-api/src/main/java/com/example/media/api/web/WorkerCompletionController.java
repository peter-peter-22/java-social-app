package com.example.media.api.web;

import com.example.media.api.upload.CompleteImageUpload;
import com.example.media.contract.worker.ImageTransformResult;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The endpoint the worker calls back with an async result. It only maps HTTP: the result becomes the
 * finalize use case's input, so a duplicate delivery finalizes idempotently on the attempt id.
 *
 * <p>The answers stop the worker's retries: success (including a duplicate delivery) is 204, a
 * result the attempt can never accept (already failed, or a different attempt) is a 4xx, and an
 * unexpected failure is 500.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class WorkerCompletionController {
    private final CompleteImageUpload completeImageUpload;

    @PostMapping(path = "/completion/image", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> complete(@RequestBody ImageTransformResult result) {
        completeImageUpload.complete(result.attemptId(), result);
        return ResponseEntity.noContent().build();
    }

    /** A result the attempt can never accept: stop the worker's retries. */
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Void> cannotComplete(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
