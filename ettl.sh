#!/usr/bin/env bash

# -help
# ettl.sh --imagesBasePath /home/pi/dev/expose-to-the-light/captured-images/ --setSettings --numberOfCaptures 5 --intervalSeconds 5 --rawFileExtension CR2

artifact="expose-to-the-light_2.13-0.1.6.jar"

shift
args=$*

export LOG_LOCATION=/tmp/ettl/log

echo "Starting application..."
java -jar "$artifact" "$args"
