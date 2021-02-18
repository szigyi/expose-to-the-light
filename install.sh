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
version_url_map["0.1.2"]="https://www.dropbox.com/s/mhcdacxptw8yfyy/expose-to-the-light_2.13-0.1.2.jar?dl=0"
version_url_map["0.1.3"]="https://www.dropbox.com/s/7fxahbugo7ky145/expose-to-the-light_2.13-0.1.3.jar?dl=0"
version_url_map["0.1.4"]="https://www.dropbox.com/s/kythpz1w9nu968c/expose-to-the-light_2.13-0.1.4.jar?dl=0"
version_url_map["0.1.5"]="https://www.dropbox.com/s/9prulf84m9k1fh0/expose-to-the-light_2.13-0.1.5.jar?dl=0"
##_new_version_url_map_here

echo "Downloading artifact..."
wget -O "$artifact" ${version_url_map["$version"]}

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing auto-runner..."

