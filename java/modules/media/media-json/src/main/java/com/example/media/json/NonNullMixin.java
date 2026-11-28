package com.example.media.json;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Omits null properties of the nested contract records. The domain carries no Jackson annotations,
 * so the persisted JSON shape keeps its absent values absent instead of writing explicit nulls.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class NonNullMixin {
}
