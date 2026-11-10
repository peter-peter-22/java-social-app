package com.example.object_storage.regional.args;

import com.example.object_storage.tags.ObjectTag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.Collection;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PutLocalObjectArgs extends LocalObjectArgs {
    private final InputStream inputStream;
    private final String contentType;
    private final long contentLength;
    @Nullable
    private final Collection<ObjectTag> tags;
}
