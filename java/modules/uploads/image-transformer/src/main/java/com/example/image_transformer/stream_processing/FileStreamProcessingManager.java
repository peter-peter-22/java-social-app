package com.example.image_transformer.stream_processing;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class FileStreamProcessingManager {
    public byte[] readAllBytes(@NonNull Supplier<@NonNull InputStream> source) {
        try (var inputStream = source.get()) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file stream", e);
        }
    }

    public void process(
            byte @NonNull [] source,
            @NonNull Function<@NonNull InputStream, @NonNull InputStream> processor,
            @NonNull Consumer<@NonNull InputStream> consumer
    ) {
        try (var inputStream = new ByteArrayInputStream(source);
             var outputStream = processor.apply(inputStream)) {
            consumer.accept(outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process file stream", e);
        }
    }
}
