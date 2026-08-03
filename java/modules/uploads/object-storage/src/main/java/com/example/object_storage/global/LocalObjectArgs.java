package com.example.object_storage.global;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@EqualsAndHashCode
@SuperBuilder
public class LocalObjectArgs {
    private final String key;
    private final String bucket;
}
