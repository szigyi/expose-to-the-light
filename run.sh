#!/usr/bin/env bash

# -help
# run.sh 0.1.4 --imagesBasePath /home/pi/dev/expose-to-the-light/captured-images/ --setSettings --numberOfCaptures 5 --intervalSeconds 5

version=$1
artifact="expose-to-the-light_2.13-"$version".jar"

shift
args=$*

echo "Starting application..."
java -jar "$artifact" "$args"
