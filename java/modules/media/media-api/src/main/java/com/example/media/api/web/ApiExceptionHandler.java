package com.example.media.api.web;

import com.example.media.api.read.VariantNotFoundException;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.transformation.TransformationNotFoundException;
import com.example.media.contract.upload.AttemptNotActiveException;
import com.example.media.contract.upload.UploadAlreadyFinishedException;
import com.example.media.contract.upload.UploadKeyTakenException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Delivers the use case outcomes as HTTP: a missing upload or variant is 404, a request that can
 * never succeed as sent is 400, and an unexpected failure is 500. No catch-all turns a server
 * error into a 404. The advice covers only this application's controllers, so the framework's own
 * answers for unmatched paths and methods keep working.
 */
@Slf4j
@NullMarked
@RestControllerAdvice(basePackages = "com.example.media.api")
public class ApiExceptionHandler {

    @ExceptionHandler({UploadNotFoundException.class, VariantNotFoundException.class})
    ResponseEntity<Void> notFound(RuntimeException exception) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            TransformationNotFoundException.class,
            UploadAlreadyFinishedException.class,
            UploadKeyTakenException.class,
            AttemptNotActiveException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<Void> badRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Void> unexpected(Exception exception) {
        log.error("Unexpected failure while serving a media API request", exception);
        return ResponseEntity.internalServerError().build();
    }
}