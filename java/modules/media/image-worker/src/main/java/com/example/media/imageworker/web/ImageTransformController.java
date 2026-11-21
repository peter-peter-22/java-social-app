package com.example.media.imageworker.web;

import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.imageworker.transform.TransformImage;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The worker's transform endpoint. It only maps HTTP: the command becomes the use case's input and
 * the result becomes the response body, both in the JSON shape the contract defines. This is the
 * endpoint the media API's blocking {@code ImageTransformer} call posts to
 * ({@code com.example.media.api.worker.HttpImageTransformer}).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ImageTransformController {
    private final TransformImage transformImage;

    @PostMapping(
            path = "/transform/image",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ImageTransformResult transform(@RequestBody ImageTransformCommand command) {
        return transformImage.transform(command);
    }
}