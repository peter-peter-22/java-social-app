package com.example.object_storage.tags;

import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * One object tag: a single non-null key/value pair. This is the only tag representation of the
 * adapter; the tag names and values stay here and never reach the application services, which only
 * see {@link com.example.media.contract.object.ObjectLifetime}.
 */
@NullMarked
public record ObjectTag(String key, String value) {
    /** The tag the uploads bucket lifecycle rule matches on: such objects expire. */
    public static final ObjectTag EXPIRES = new ObjectTag("expires", "enabled");
    /** The tag of kept objects: the lifecycle rule ignores them, so they no longer expire. */
    public static final ObjectTag PERMANENT = new ObjectTag("expires", "disabled");

    /** The Minio SDK takes the tags of a write as a map. */
    public Map<String, String> asMap() {
        return Map.of(key, value);
    }
}
