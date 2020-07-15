#!/usr/bin/env bash

token=$1
version=$2

echo ""
echo "Assembling $version jar package..."
sbt clean assembly

echo ""
echo "Deploying $version jar package to Dropbox..."
asset_src="target/scala-2.13/expose-to-the-light_2.13-$version.jar"
asset_dst="/artifact/expose-to-the-light_2.13-$version.jar"
install_src="install.sh"
install_dst="/script/install_$version.sh"

curl -X POST https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$asset_dst\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$asset_src

echo ""
echo "Deploying $version install script..."
curl -X POST https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$install_dst\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$install_src