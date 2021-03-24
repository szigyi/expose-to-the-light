#!/usr/bin/env bash

replace_string() {
  placeholder=$1
  new_string=$2
  file_name=$3
  ESCAPED_REPLACE=$(printf '%s\n' "$new_string" | sed -e 's/[\/&]/\\&/g')

  sed -i '' -e "s/$placeholder/$ESCAPED_REPLACE/" "$file_name"
}

artifact_link="https://www.dropbox.com/s/fog3rm3g6qayrzk/expose-to-the-light_2.13-0.1.15.jar?dl=1"
ettl_link="https://www.dropbox.com/s/l7terzf8756mwqm/ettl?dl=1"
artifact="expose-to-the-light_2.13-0.1.15.jar"

echo "Creating app folder at /usr/local/opt/ettl"
sudo mkdir -p /usr/local/opt/ettl
sudo chown -R pi /usr/local/opt/ettl
cd /usr/local/opt/ettl

echo "Downloading artifact to /usr/local/opt/ettl..."
curl -L -o "$artifact" "$artifact_link"

echo "Downloading ettl script to /usr/local/opt/ettl..."
curl -L -o "ettl" "$ettl_link"

sudo chmod u+x ettl

echo "Artifact's Manifest file:"
jar xf "$artifact" META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF

echo "Installing and adding to PATH..."

# make the artifact path absolute
replace_string "artif=.*" "artif=\"/usr/local/opt/ettl/$artifact\"" "ettl"

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  echo "Detected OS is Linux"
  echo "Installing ettl command to /usr/local/bin"
  cd /usr/local/bin
  sudo ln -fs /usr/local/opt/ettl/ettl ettl
  sudo chown -R pi /usr/local/bin/ettl

  echo "Installing dependencies"
  if ! command -v java $> /dev/null
  then
    echo "java is missing, therefore installing java 11"
    sudo apt-get -y install openjdk-11-jdk
  fi
  if ! command -v gphoto2 $> /dev/null
  then
    echo "gphoto2 is missing, therefore installing gphoto2"
    # https://github.com/gonzalo/gphoto2-updater
#    sudo wget https://raw.githubusercontent.com/gonzalo/gphoto2-updater/master/gphoto2-updater.sh && chmod +x gphoto2-updater.sh && sudo ./gphoto2-updater.sh
    sudo apt -y install gphoto2
    sudo apt -y install libgphoto2-6
  fi

  source ~/.bashrc

elif [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Detected OS is macOS"
  echo "Installing application to /usr/local/opt/ettl"
  replace_string "vm_options=.*" "vm_options=\"-Djna.library.path=/usr/local/Cellar/libgphoto2/2.5.27/lib\"" "ettl"

  echo "Installing ettl command to /usr/local/bin"
  cd /usr/local/bin
  sudo ln -fs /usr/local/opt/ettl/ettl ettl

  echo "Installing dependencies"
  brew install gphoto2
  brew install libgphoto2

  source "$ZSH"/oh-my-zsh.sh

else
  echo "big problemo! No OSTYPE"
fi
