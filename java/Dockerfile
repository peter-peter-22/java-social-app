FROM eclipse-temurin:24-jdk-noble

WORKDIR /workspace

# Installing libvips and jemalloc
# jemalloc is installed for better memory management
# (recommended by the official docs of vipsffm https://github.com/lopcode/vips-ffm?tab=readme-ov-file#memory-allocation)

RUN apt-get update && apt-get install -y --no-install-recommends libjemalloc2 libvips42 kotlin && \
    rm -rf /var/lib/apt/lists/*

# Enable jemalloc for all processes in the container
# It's standard and recommended for JVM + libvips (and all environments with no significant independent processes)
ENV LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libjemalloc.so.2

# Optional: Fine-tune jemalloc behavior (very useful)
# This config uses fast(er) memory re-claiming to reduce idle memory at the cost of sligtly more CPU usage.
# dirty_decay_ms:1000 = how quickly the "dirty pages" (allocated but unused memory) are returned purged. default: 10_000.
# muzzy_decay_ms:1000 = how quickly the "muzzy pages" (pages those were marked for purge but are not re-used yet) are returned to usage. default: 10_000.
# background_thread:true = use background threads instead of the main thread for memory management.
ENV MALLOC_CONF="dirty_decay_ms:1000,muzzy_decay_ms:1000,background_thread:true"

# Testing jemalloc usage:
# ldd /path/to/your/java | grep jemalloc   # or check /proc/<pid>/maps
# or
# java -XshowSettings:properties -version 2>&1 | grep malloc

ENV GRADLE_USER_HOME=/gradle-cache

RUN mkdir -p "${GRADLE_USER_HOME}" && chmod 0777 "${GRADLE_USER_HOME}"

CMD ["./gradlew", "--no-daemon", ":modules:media:image_transformer:test"]