package com.example.media.api.web;

import com.example.media.contract.transformation.TransformationSpec;

/** The registered named transformation at its current version. */
public record AdminTransformationResponse(String name, TransformationSpec spec) {
}
