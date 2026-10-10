package com.example.upload_registry.derived;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DerivedCrudRepository extends JpaRepository<DerivedEntity, UUID> {
}
