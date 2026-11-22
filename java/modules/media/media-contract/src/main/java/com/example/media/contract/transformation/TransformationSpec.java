package com.example.media.contract.transformation;

/**
 * The resolved operations of a transformation.
 * The JSON shape is persisted in the registry (jsonb) and sent to workers; it is part of this
 * contract and must not drift. The shape is defined by the mixins in {@code media-json}: the type
 * property is "type", and the discriminator values are "image" and "video".
 */
public sealed interface TransformationSpec permits ImageTransformationSpec, VideoTransformationSpec {
}