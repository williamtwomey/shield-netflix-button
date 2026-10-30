#!/bin/sh
# Build + sign the SHIELD Netflix-button stub. Output: out/netflix-stub.apk
set -e
cd "$(dirname "$0")"
SDK=${ANDROID_HOME:-$HOME/Android/Sdk}
BT=$SDK/build-tools/36.0.0
AJ=$SDK/platforms/android-35/android.jar
JDK=$(dirname "$(readlink -f "$(command -v javac)")")
VERSION_CODE=${VERSION_CODE:-1}
VERSION_NAME=${VERSION_NAME:-stub-$VERSION_CODE}

# Signing key: a throwaway one is created on first build if none is present. Releases use
# the real key, which is not in the repo (see BUILDING.md).
if [ ! -f stub.jks ]; then
    head -c 24 /dev/urandom | base64 | tr -d "/+=" > stub.jks.pass
    chmod 600 stub.jks.pass
    $JDK/keytool -genkeypair -keystore stub.jks -alias stub -keyalg RSA -keysize 2048 \
        -validity 10000 -dname "CN=shield-netflix-stub" \
        -storepass "$(cat stub.jks.pass)" -keypass "$(cat stub.jks.pass)" >/dev/null
fi

rm -rf out && mkdir -p out/classes
$BT/aapt2 compile --dir res -o out/res.zip
$BT/aapt2 link -o out/unsigned.apk -I "$AJ" --manifest AndroidManifest.xml -R out/res.zip \
    --min-sdk-version 21 --target-sdk-version 30 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME"
$JDK/javac -nowarn --release 11 -cp "$AJ" -d out/classes src/com/netflix/ninja/*.java
$BT/d8 --release --min-api 21 --lib "$AJ" --output out out/classes/com/netflix/ninja/*.class
(cd out && zip -q -j unsigned.apk classes.dex)
$BT/zipalign -f 4 out/unsigned.apk out/aligned.apk
$BT/apksigner sign --ks stub.jks --ks-pass file:stub.jks.pass --out out/netflix-stub.apk out/aligned.apk
$BT/apksigner verify out/netflix-stub.apk
sha256sum out/netflix-stub.apk
