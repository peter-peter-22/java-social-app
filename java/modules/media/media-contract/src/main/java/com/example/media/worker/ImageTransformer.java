package com.example.media.worker;

import org.jspecify.annotations.NullMarked;

/**
 * Blocking image transform. {@code enqueue} is added later and must send the same {@link ImageWork}.
 * The worker downloads, transforms, uploads under the reserved prefixes, and returns metadata.
 * It does not insert rows, make objects permanent, or finalize the attempt.
 */
@NullMarked
public interface ImageTransformer {
    ImageWorkResult transform(ImageWork work);
}
