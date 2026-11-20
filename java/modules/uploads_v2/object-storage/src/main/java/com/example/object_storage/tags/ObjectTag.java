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
    // TODO there must be a better way to store the tags
    EXPIRES("expires","enabled"),
    PERMANENT("expires","disabled");

    private final String key;
    private final String value;

    public static Map<String, String> toMap(Collection<ObjectTag> tags) {
        return tags.stream().collect(Collectors.toMap(ObjectTag::getKey, ObjectTag::getValue));
    }
}
