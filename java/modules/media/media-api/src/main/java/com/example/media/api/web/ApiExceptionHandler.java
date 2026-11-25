package com.example.media.api.web;

import com.example.media.api.read.VariantNotFoundException;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.transformation.TransformationNotFoundException;
import com.example.media.contract.upload.AttemptNotActiveException;
import com.example.media.contract.upload.UploadAlreadyFinishedException;
import com.example.media.contract.upload.UploadKeyTakenException;
import com.example.media.contract.upload.UploadPermissionDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Delivers the use case outcomes as HTTP: a missing upload or variant is 404, a request that can
 * never succeed as sent is 400, a denied upload permission is 403, and an unexpected failure is
 * 500. No catch-all turns a server error into a 404. The advice covers only this application's
 * controllers, so the framework's own answers for unmatched paths and methods keep working.
 */
@Slf4j
@NullMarked
@RestControllerAdvice(basePackages = "com.example.media.api")
public class ApiExceptionHandler {

    @ExceptionHandler({UploadNotFoundException.class, VariantNotFoundException.class})
    ResponseEntity<Void> notFound(RuntimeException exception) {
        return ResponseEntity.notFound().build();
    }

    /**
     * A denied permission is 403: the request is well formed but its credential does not authorize
     * it. Expired and exhausted permissions are subclasses of the denied exception.
     */
    @ExceptionHandler(UploadPermissionDeniedException.class)
    ResponseEntity<Void> forbidden(UploadPermissionDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            TransformationNotFoundException.class,
            UploadAlreadyFinishedException.class,
            UploadKeyTakenException.class,
            AttemptNotActiveException.class,
            MissingServletRequestPartException.class,
            HttpMessageConversionException.class
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