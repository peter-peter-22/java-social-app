package com.example.object_storage.global;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.InputStream;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PutGlobalObjectArgs extends LocalObjectArgs {
    private final InputStream inputStream;
    private final String contentType;
    private final long contentLength;
}
