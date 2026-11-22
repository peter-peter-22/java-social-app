package com.example.object_storage.regional.args;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NullMarked;

@Getter
@EqualsAndHashCode
@SuperBuilder
@NullMarked
public class LocalObjectArgs {
    private final String key;
    private final String bucket;
}
