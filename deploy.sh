#!/usr/bin/env bash

token=$1
buildNumber=$2
export BUILD_NUMBER="$buildNumber"
version="0.1.$buildNumber"

echo ""
echo "Assembling $version jar package..."
sbt clean assembly

echo ""
echo "Deploying $version jar package to Dropbox..."
artifact="expose-to-the-light_2.13-$version.jar"
asset_src="target/scala-2.13/$artifact"
asset_dst="/artifact/$artifact"
install_src="install.sh"
install_dst="/script/install.sh"
ettl_src="ettl"
ettl_dst="/script/ettl"

curl https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$asset_dst\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$asset_src

echo ""
echo "Making uploaded artifact public..."
shared_link=$(
  curl https://api.dropboxapi.com/2/sharing/create_shared_link_with_settings \
      --header "Authorization: Bearer $token" \
      --header "Content-Type: application/json" \
      --data "{\"path\": \"$asset_dst\",\"settings\": {\"requested_visibility\": \"public\"}}"  | jq -r '.url')

echo "Shared Link:"
echo "$shared_link"

replace_string() {
  placeholder=$1
  new_string=$2
  file_name=$3
  ESCAPED_REPLACE=$(printf '%s\n' "$new_string" | sed -e 's/[\/&]/\\&/g')

  sed -i '' -e "s/$placeholder/$ESCAPED_REPLACE/" "$file_name"
}

echo ""
echo "Updating install and ettl scripts to use the $version version..."
replace_string "artifact=.*" "artifact=\"$artifact\"" "ettl"
replace_string "artifact=.*" "artifact=\"$artifact\"" "install.sh"
replace_string "shared_link=.*" "shared_link=\"$shared_link\"" "install.sh"


echo ""
echo "Deploying $version install script..."
curl https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$install_dst\", \"mode\": \"overwrite\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$install_src | jq -r '.path_lower'

echo ""
echo "Deploying $version ettl/run script..."
curl https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$ettl_dst\", \"mode\": \"overwrite\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$ettl_src | jq -r '.path_lower'