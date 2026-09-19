package com.example.transformer_contracts.file_repository;

import com.example.uploads_api.transformations.asset.Asset;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class ProcessedFileRepository {
    private final TransformedFileRepository transformedFileRepository;
    private final OriginalFileRepository originalFileRepository;

    public void downloadOriginal(@NonNull Path path, @NonNull Asset asset) {
        var key = asset.assetId() + asset.format().getExtension();
        originalFileRepository.get(key, path);
    }

    public void uploadTransformations(Collection<SetFileArgs> uploads) {
        transformedFileRepository.setAll(uploads);
    }
}
