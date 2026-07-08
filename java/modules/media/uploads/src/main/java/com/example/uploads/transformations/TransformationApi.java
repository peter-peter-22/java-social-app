package com.example.uploads.transformations;

import com.example.media_api.transformations.api.UploadTransformationDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

interface TransformationApi {
    void transform(@NotNull Collection<UploadTransformationDTO> transformations);
}
