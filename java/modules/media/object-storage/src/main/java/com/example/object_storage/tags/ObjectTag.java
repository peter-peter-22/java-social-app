package com.example.object_storage.tags;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@NullMarked
public enum ObjectTag {
    // TODO step 10 of clean-media-service.md replaces this enum and the nullable tag arguments
    //  with a single non-null tag representation.
    EXPIRES("expires","enabled"),
    PERMANENT("expires","disabled");

    private final String key;
    private final String value;

    public static Map<String, String> toMap(Collection<ObjectTag> tags) {
        return tags.stream().collect(Collectors.toMap(ObjectTag::getKey, ObjectTag::getValue));
    }
}
