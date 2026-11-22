package com.example.object_storage.regional.args;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.InputStream;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PutLocalObjectArgs extends SetLocalObjectArgs {
    private final InputStream inputStream;
    private final long contentLength;
}
