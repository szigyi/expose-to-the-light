#!/usr/bin/env bash

version=$1
artifact="expose-to-the-light_2.13-"$version".jar"

if [[ ! -n $(which java) ]]; then
  echo "Installing jdk as did not found it on this machine..."
  sudo apt-get update
  sudo apt-get install default-jdk
fi

declare -A version_url_map
version_url_map["0.1.1"]="https://www.dropbox.com/s/2plv7ixju61gbk7/expose-to-the-light_2.13-0.1.1.jar?dl=0"
version_url_map["0.1.2"]="https://www.dropbox.com/s/16ci64oy5tfq8dp/expose-to-the-light_2.13-0.1.2.jar?dl=0"
##_new_version_url_map_here

echo "Downloading artifact..."
wget ${version_url_map["$version"]}
mv "$artifact"?dl=1 "$artifact"

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing auto-runner..."


echo "Starting application..."
java -jar "$artifact"
