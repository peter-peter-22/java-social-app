# Build the docker image used by `run.bash`
cd "$(dirname "$0")"
docker build -t java-social-image-test -f media.Dockerfile .
