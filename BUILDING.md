# Building

No Gradle. `build.sh` calls the SDK tools directly.

## Requirements

- Android SDK with `build-tools;36.0.0` and `platforms;android-35`, at `$ANDROID_HOME`
  (default `~/Android/Sdk`)
- A JDK 11 or newer, with `javac` on `PATH`
- `zip`

## Build

```sh
./build.sh                                     # -> out/netflix-stub.apk
VERSION_CODE=10100 VERSION_NAME=1.1.0 ./build.sh
```

The defaults are `VERSION_CODE=1` and `VERSION_NAME=stub-1`.

## Signing

`build.sh` signs with `stub.jks`, using the password in `stub.jks.pass`. If neither file
exists, it generates a throwaway key. That's fine for testing. Android won't upgrade an
installed app to an APK signed with a different key, so moving between your own build and
a release build means uninstalling first (`adb uninstall com.netflix.ninja`). The key
files are gitignored.

## Releases

`.github/workflows/build.yml` builds every push and pull request and uploads the APK as a
workflow artifact. Pushing a `vX.Y.Z` tag builds a signed APK and attaches it to a GitHub
release:

```sh
git tag v1.1.0 && git push origin v1.1.0
```

The version code is computed from the tag as `X*10000 + Y*100 + Z`, so `Y` and `Z` must
stay below 100.

Tag builds sign with the release key from two repository secrets:

- `SIGNING_KEYSTORE_B64`: `base64 -w0 stub.jks`
- `SIGNING_KEYSTORE_PASSWORD`: the keystore password (the key uses the same one)

Only tag builds use those secrets. Branch and pull request builds use a throwaway key.

## Icon

`art/icon.png` is the source. To regenerate the launcher icons in `res/mipmap-*` and the
TV banner in `res/drawable-xhdpi`:

```sh
python3 art/make-icons.py
```
