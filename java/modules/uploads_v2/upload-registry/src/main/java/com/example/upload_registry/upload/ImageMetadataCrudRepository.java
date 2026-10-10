package com.example.upload_registry.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ImageMetadataCrudRepository extends JpaRepository<ImageMetadataEntity, UUID> {
}
