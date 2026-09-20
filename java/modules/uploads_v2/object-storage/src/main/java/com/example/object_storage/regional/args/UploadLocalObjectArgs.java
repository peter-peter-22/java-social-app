package com.example.object_storage.regional.args;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class UploadLocalObjectArgs extends LocalObjectArgs {
    private final String contentType;
    private final String sourcePath;
}
