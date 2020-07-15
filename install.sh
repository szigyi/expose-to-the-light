#!/usr/bin/env bash

version=$1

if [[ -n $(which java) ]]; then
  echo "Installing jdk as did not found it on this machine..."
  sudo apt-get update
  sudo apt-get install default-jdk
fi

echo "Downloading artifact..."
wget https://www.dropbox.com/s/2plv7ixju61gbk7/expose-to-the-light_2.13-"$version".jar

echo "Installing auto-runner..."


echo "Starting application..."
java -jar expose-to-the-light_2.13-"$version".jar