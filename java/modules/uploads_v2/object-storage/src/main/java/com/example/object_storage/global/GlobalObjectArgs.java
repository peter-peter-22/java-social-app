package com.example.object_storage.global;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GlobalObjectArgs extends LocalObjectArgs {
    private final String homeRegion;
}
