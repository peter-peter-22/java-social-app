package com.example.media.registry;

import com.example.media.json.MediaJson;
import org.hibernate.type.format.AbstractJsonFormatMapper;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

/**
 * The jsonb transformation specs speak the media service's standard JSON shape: every persisted
 * value is serialized through {@link MediaJson}, the same configuration the web and the future
 * message queue use. Hibernate 7.2 ships only a Jackson 2 format mapper, so the shared Jackson 3
 * mapper is bridged in here instead.
 */
public class MediaJsonFormatMapper extends AbstractJsonFormatMapper {

    private final JsonMapper jsonMapper = MediaJson.create();

    @Override
    protected <T> T fromString(CharSequence charSequence, Type type) {
        return jsonMapper.readValue(charSequence.toString(), jsonMapper.getTypeFactory().constructType(type));
    }

    @Override
    protected <T> String toString(T value, Type type) {
        return jsonMapper.writeValueAsString(value);
    }
}