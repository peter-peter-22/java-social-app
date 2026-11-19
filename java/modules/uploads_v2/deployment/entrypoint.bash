#!/bin/bash

echo "entrypoint workdir:"
pwd
echo "workdir contents:"
ls
echo "available jar files:"
find "/" -type f -name "*.jar"

exec "$@"
