#!/usr/bin/env bash

version=$1
artifact="expose-to-the-light_2.13-"$version".jar"

if [[ ! -n $(which java) ]]; then
  echo "Installing jdk as did not found it on this machine..."
  sudo apt-get update
  sudo apt-get install default-jdk
fi

echo "Downloading artifact..."
wget https://www.dropbox.com/s/2plv7ixju61gbk7/"$artifact"?dl=1
mv "$artifact"?dl=1 "$artifact"

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing auto-runner..."


echo "Starting application..."
java -jar "$artifact"