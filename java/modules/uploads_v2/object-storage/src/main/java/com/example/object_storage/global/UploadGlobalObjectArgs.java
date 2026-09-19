package com.example.object_storage.global;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class UploadGlobalObjectArgs extends LocalObjectArgs {
    private final String contentType;
    private final Path source;
}
