#!/usr/bin/env bash

# -help
# run.sh 0.1.4 /home/pi/dev/expose-to-the-light/captured-images/

version=$1
artifact="expose-to-the-light_2.13-"$version".jar"

base_path=$2
mkdir "$base_path"

echo "Starting application..."
java -jar "$artifact" "$base_path"
