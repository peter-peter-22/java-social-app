docker run --rm -it \
  -v "$PWD/../../../..":/workspace \
  -v java-social-gradle-1:/root/.gradle \
  -w /workspace \
  eclipse-temurin:25-jdk-noble \
  bash -lc 'apt-get update && apt-get install -y --no-install-recommends libvips42t64 ffmpeg && ./gradlew --no-daemon :image-transformer:integrationTest'