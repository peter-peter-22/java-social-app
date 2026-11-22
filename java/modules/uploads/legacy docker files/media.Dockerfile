# Development dockerfile for running java codes those require ffmpeg or lipvips

FROM eclipse-temurin:25-jdk-noble

WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends libvips42t64 ffmpeg

COPY entrypoint.bash /entrypoint.bash
RUN chmod 755 /entrypoint.bash
ENTRYPOINT ["/entrypoint.bash"]

