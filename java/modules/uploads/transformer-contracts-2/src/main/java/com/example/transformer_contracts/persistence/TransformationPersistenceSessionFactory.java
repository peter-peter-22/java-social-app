package com.example.transformer_contracts.persistence;

import com.example.transformer_contracts.file_repository.ProcessedFileRepository;
import com.example.uploads_api.transformations.asset.Asset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransformationPersistenceSessionFactory {
    private final ProcessedFileRepository processedFileRepository;

    public TransformationPersistenceSession createSession(Asset asset) {
        return new TransformationPersistenceSession(asset, processedFileRepository);
    }
}
