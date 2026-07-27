package com.example.video_transformer.operations;

import com.example.uploads_api.transformations.operations.VideoTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class VideoTransformationService {
    public @NonNull InputStream transformFile(@NonNull InputStream inputStream, @NonNull VideoTransformationOperations operations) {
        return inputStream;
    }
}
