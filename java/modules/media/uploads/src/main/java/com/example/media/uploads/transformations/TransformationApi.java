package com.example.media.uploads.transformations;

import com.example.media.api.transformations.api.UploadTransformationDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

interface TransformationApi {
    void transform(@NotNull Collection<UploadTransformationDTO> transformations);
}
