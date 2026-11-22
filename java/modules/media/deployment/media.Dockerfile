# Development dockerfile for running java codes those require ffmpeg or lipvips

FROM eclipse-temurin:25-jdk-noble

WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends libvips42t64 ffmpeg

