#!/usr/bin/env bash

version=$1
artifact="expose-to-the-light_2.13-"$version".jar"

echo "Starting application..."
java -jar "$artifact"
