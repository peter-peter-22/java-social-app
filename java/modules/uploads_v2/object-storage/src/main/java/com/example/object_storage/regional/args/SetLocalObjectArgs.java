package com.example.object_storage.regional.args;

import com.example.object_storage.tags.ObjectTag;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SetLocalObjectArgs extends LocalObjectArgs {
    private final String contentType;
    @Nullable
    private final Collection<ObjectTag> tags;
    @Builder.Default
    private final boolean allowOverwrite = false;
}
