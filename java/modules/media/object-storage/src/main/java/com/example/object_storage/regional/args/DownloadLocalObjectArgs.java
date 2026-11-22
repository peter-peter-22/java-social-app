package com.example.object_storage.regional.args;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class DownloadLocalObjectArgs extends LocalObjectArgs {
    private final String destinationPath;
}
