#!/usr/bin/env bash

replace_string() {
  placeholder=$1
  new_string=$2
  file_name=$3
  ESCAPED_REPLACE=$(printf '%s\n' "$new_string" | sed -e 's/[\/&]/\\&/g')

  sed -i '' -e "s/$placeholder/$ESCAPED_REPLACE/" "$file_name"
}

artifact_link="https://www.dropbox.com/s/9nzw7u4ku8mzt1y/expose-to-the-light_2.13-0.1.10.jar?dl=1"
ettl_link="https://www.dropbox.com/s/cs7ozhei0seffz3/ettl?dl=1"
artifact="expose-to-the-light_2.13-0.1.10.jar"

echo "Downloading artifact..."
curl -L -o "$artifact" "$artifact_link"

echo "Downloading ettl script..."
curl -L -o "ettl" "$ettl_link"

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing and adding to PATH..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  echo "linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Detected OS is macOS"
  echo "Installing application to /usr/local/opt/ettl"
  mkdir -p /usr/local/opt/ettl
  cp "$artifact" /usr/local/opt/ettl
  cp ettl /usr/local/opt/ettl
  rm "$artifact"
  rm ettl

  cd /usr/local/opt/ettl
  replace_string "artif=.*" "artif=\"/usr/local/opt/ettl/$artifact\"" "ettl"
  replace_string "vm_options=.*" "vm_options=\"-Djna.library.path=/usr/local/Cellar/libgphoto2/2.5.27/lib\"" "ettl"

  echo "Installing ettl command to /usr/local/bin"
  cd /usr/local/bin
  ln -fs /usr/local/opt/ettl/ettl ettl

  echo "Installing dependencies"
  brew install gphoto2
  brew install libgphoto2
elif [[ "$OSTYPE" == "freebsd"* ]]; then
  echo "freebsd"
else
  echo "big problemo! No OSTYPE"
fi

source "$ZSH"/oh-my-zsh.sh
