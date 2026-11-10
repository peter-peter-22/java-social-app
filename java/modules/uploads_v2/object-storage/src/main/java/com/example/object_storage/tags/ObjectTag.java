package com.example.object_storage.tags;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ObjectTag {
    EXPIRES("expires","enabled");

    private final String key;
    private final String value;

    public static Map<String, String> toMap(Collection<ObjectTag> tags) {
        return tags.stream().collect(Collectors.toMap(ObjectTag::getKey, ObjectTag::getValue));
    }
}
