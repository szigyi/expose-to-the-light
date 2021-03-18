#!/usr/bin/env bash

replace_string() {
  placeholder=$1
  new_string=$2
  file_name=$3
  ESCAPED_REPLACE=$(printf '%s\n' "$new_string" | sed -e 's/[\/&]/\\&/g')

  sed -i '' -e "s/$placeholder/$ESCAPED_REPLACE/" "$file_name"
}

shared_link="https://www.dropbox.com/s/paae4l23u1du6ds/expose-to-the-light_2.13-0.1.7.jar?dl=1"
artifact="expose-to-the-light_2.13-0.1.7.jar"

echo "Downloading artifact..."
curl -L -o "$artifact" "$shared_link"

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing and adding to PATH..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  echo "linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Install to macOS"
  mkdir -p /usr/local/opt/ettl
  cp "$artifact" /usr/local/opt/ettl
  cp ettl /usr/local/opt/ettl

  cd /usr/local/opt/ettl
  replace_string "artif=.*" "artif=\"/usr/local/opt/ettl/$artifact\"" "ettl"

  cd /usr/local/bin
  ln -fs /usr/local/opt/ettl/ettl ettl
elif [[ "$OSTYPE" == "freebsd"* ]]; then
  echo "freebsd"
else
  echo "big problemo! No OSTYPE"
fi

source "$ZSH"/oh-my-zsh.sh
