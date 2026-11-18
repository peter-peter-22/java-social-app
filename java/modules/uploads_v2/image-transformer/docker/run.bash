docker run --rm -it \
  -v "$PWD/../../../..":/workspace \
  -v java-social-image-test-gradle-2:/root/.gradle \
  -w /workspace \
  java-social-image-test \
  ./gradlew --no-daemon :image-transformer:integrationTest