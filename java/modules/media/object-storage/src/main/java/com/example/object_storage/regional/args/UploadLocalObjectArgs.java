package com.example.object_storage.regional.args;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class UploadLocalObjectArgs extends SetLocalObjectArgs {
    private final String sourcePath;
}
