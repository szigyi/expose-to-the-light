#!/usr/bin/env bash

shared_link="https://www.dropbox.com/s/2yfi8nrp255ym15/expose-to-the-light_2.13-0.1.6.jar?dl=0"
artifact="expose-to-the-light_2.13-0.1.6.jar"

echo "Downloading artifact..."
curl "$shared_link" -o "$artifact"

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing auto-runner..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  echo "linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "macOS"
  mkdir -p /usr/local/opt/ettl
  cp "$artifact" /usr/local/opt/ettl
  cp ettl.sh /usr/local/opt/ettl
  ln -s ettl.sh /usr/local/bin/ettl.sh
elif [[ "$OSTYPE" == "freebsd"* ]]; then
  echo "freebsd"
else
  echo "big problemo! No OSTYPE"
fi
