package com.example.object_storage.regional.args;

import com.example.object_storage.tags.ObjectTag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class UploadLocalObjectArgs extends LocalObjectArgs {
    private final String contentType;
    private final String sourcePath;
    @Nullable
    private final Collection<ObjectTag> tags;
}
