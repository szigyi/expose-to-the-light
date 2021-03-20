#!/usr/bin/env bash

replace_string() {
  placeholder=$1
  new_string=$2
  file_name=$3
  ESCAPED_REPLACE=$(printf '%s\n' "$new_string" | sed -e 's/[\/&]/\\&/g')

  sed -i '' -e "s/$placeholder/$ESCAPED_REPLACE/" "$file_name"
}

delete_file() {
  token=$1
  destination=$2
  curl https://api.dropboxapi.com/2/files/delete_v2 \
      --header "Authorization: Bearer $token" \
      --header "Content-Type: application/json" \
      --data "{\"path\": \"$destination\"}" | jq -r '.path_lower'
}

upload_artifact() {
  token=$1
  source=$2
  destination=$3
  curl https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$destination\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$source | jq -r '.path_lower'
}

upload_script_file() {
  token=$1
  source=$2
  destination=$3
  curl https://content.dropboxapi.com/2/files/upload \
    --header "Authorization: Bearer $token" \
    --header "Dropbox-API-Arg: {\"path\": \"$destination\", \"mode\": \"overwrite\"}" \
    --header "Content-Type: application/octet-stream" \
    --data-binary @$source | jq -r '.path_lower'
}

make_file_public() {
  token=$1
  destination=$2
  artifact_link=$(curl https://api.dropboxapi.com/2/sharing/create_shared_link_with_settings \
      --header "Authorization: Bearer $token" \
      --header "Content-Type: application/json" \
      --data "{\"path\": \"$destination\",\"settings\": {\"requested_visibility\": \"public\"}}" | jq -r '.url')

  # making the link downloadable
  artifact_link="$(echo "$artifact_link" | sed 's/.$//')1"
  echo "$artifact_link"
}

token=$1
buildNumber=$2
export BUILD_NUMBER="$buildNumber"
version="0.1.$buildNumber"

artifact="expose-to-the-light_2.13-$version.jar"
asset_src="target/scala-2.13/$artifact"
asset_dst="/artifact/$artifact"
install_src="install.sh"
install_dst="/script/install.sh"
ettl_src="ettl"
ettl_dst="/script/ettl"

echo ""
echo "Assembling $version jar package..."
sbt clean assembly

echo ""
echo "Deploying $version jar package to Dropbox..."
upload_artifact "$token" "$asset_src" "$asset_dst"

echo ""
echo "Making uploaded artifact public..."
artifact_link=$(make_file_public "$token" "$asset_dst")
echo "$artifact_link"

echo ""
echo "Updating ettl script to use the $version version..."
replace_string "artif=.*" "artif=\"$artifact\"" "ettl"

echo ""
echo "Deploying $version ettl/run script..."
delete_file "$token" "$ettl_dst"
upload_script_file "$token" "$ettl_src" "$ettl_dst"

echo ""
echo "Making uploaded ettl/run script public..."
ettl_link=$(make_file_public "$token" "$ettl_dst")
echo "$ettl_link"

echo ""
echo "Updating install script to use the $version version..."
replace_string "artifact=.*" "artifact=\"$artifact\"" "install.sh"
replace_string "artifact_link=.*" "artifact_link=\"$artifact_link\"" "install.sh"
replace_string "ettl_link=.*" "ettl_link=\"$ettl_link\"" "install.sh"

echo ""
echo "Deploying $version install script..."
delete_file "$token" "$install_dst"
upload_script_file "$token" "$install_src" "$install_dst"

echo ""
echo "Making uploaded install script public..."
install_link=$(make_file_public "$token" "$install_dst")
echo "$install_link"

echo ""
echo "Updating README to show the link of $version version..."
replace_string "\`curl -L -o install.sh .*" "\`curl -L -o install.sh $install_link\`" "README.md"

echo ""
echo "$version is deployed"