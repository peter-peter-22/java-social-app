package com.example.image_transformer.stream_processing;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class FileStreamProcessingManager {
   public void process(
           @NonNull Supplier<@NonNull InputStream> source,
           @NonNull Function<@NonNull InputStream, @NonNull InputStream> processor,
           @NonNull Consumer<@NonNull InputStream> consumer
   ){
      try (var inputStream = source.get()) {
         try (var outputStream = processor.apply(inputStream)) {
            consumer.accept(outputStream);
         }
      } catch (Exception e) {
         throw new RuntimeException("Failed to process file stream", e);
      }
   }
}
