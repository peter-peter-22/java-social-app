package com.example.uploads.upload_repository;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface UploadCrudRepository extends CrudRepository<UploadEntity, UUID> {
}
