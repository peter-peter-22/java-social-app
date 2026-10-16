package com.example.upload_registry.stored_asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoMetadataCrudRepository extends JpaRepository<VideoMetadataEntity, UUID> {
}
