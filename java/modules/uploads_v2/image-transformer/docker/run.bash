docker run --rm -it \
  --user "$(id -u):$(id -g)" \
  -e HOME=/tmp/home \
  -v "$PWD/../../../..":/workspace \
  -v java-social-image-test-gradle:/tmp/home/.gradle \
  -w /workspace \
  java-social-image-test \
  ./gradlew --no-daemon :image-transformer:integrationTest
  # TODO prevent gradle and the dependencies from being re-downloaded every time