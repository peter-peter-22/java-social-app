package com.example.upload_registry.variant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantCrudRepository extends JpaRepository<VariantEntity, VariantEntityId> {
}
