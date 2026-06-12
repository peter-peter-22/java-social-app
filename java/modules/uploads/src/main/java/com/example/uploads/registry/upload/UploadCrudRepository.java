package com.example.uploads.registry.upload;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface UploadCrudRepository extends CrudRepository<UploadEntity, UUID> {
}
