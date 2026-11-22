package com.example.object_storage.regional.args;

import com.example.object_storage.tags.ObjectTag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class SetTagsForLocalObjectArgs extends LocalObjectArgs {
    Collection<ObjectTag> tags;
}
