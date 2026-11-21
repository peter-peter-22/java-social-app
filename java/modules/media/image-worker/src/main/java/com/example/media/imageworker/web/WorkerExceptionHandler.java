package com.example.media.imageworker.web;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Delivers the worker outcomes as HTTP: a command that can never succeed as sent is 400 and an
 * unexpected failure is 500. No catch-all turns a processing failure into a client error; the
 * caller classifies the answer from there. The advice covers only this application's controllers.
 */
@Slf4j
@NullMarked
@RestControllerAdvice(basePackages = "com.example.media.imageworker")
public class WorkerExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Void> badRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Void> unexpected(Exception exception) {
        log.error("Unexpected failure while transforming an image", exception);
        return ResponseEntity.internalServerError().build();
    }
}