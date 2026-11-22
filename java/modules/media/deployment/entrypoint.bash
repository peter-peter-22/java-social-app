#!/bin/bash

# This docker entrypoint file adds additional debug to java jar containers.
#  It has no functional impact.

echo "entrypoint workdir:"
pwd
echo "workdir contents:"
ls
echo "available jar files:"
find "./" -type f -name "*.jar"

exec "$@"
