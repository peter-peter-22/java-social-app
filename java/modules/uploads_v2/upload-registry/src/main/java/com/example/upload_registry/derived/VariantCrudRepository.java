package com.example.upload_registry.derived;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantCrudRepository extends JpaRepository<VariantEntity, VariantId> {
}
