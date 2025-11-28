#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

# Termux provisioning script for building the Android app
# Usage:
#   bash scripts/termux-setup.sh            # install deps + Android SDK + Gradle wrapper cache
#   bash scripts/termux-setup.sh --no-build # skip Gradle build step
#
# Safe to re-run; it will reuse existing SDK/tooling when present.

BUILD_APP=1
if [[ "${1-}" == "--no-build" ]]; then
  BUILD_APP=0
fi

# 1) Base packages
pkg update -y
pkg upgrade -y
pkg install -y git curl wget unzip zip proot-distro proot tar openssl-tool \
  openjdk-17 gradle python

export JAVA_HOME="${JAVA_HOME:-/data/data/com.termux/files/usr/lib/jvm/openjdk-17}"
export PATH="$JAVA_HOME/bin:$PATH"

# 2) Android SDK command-line tools
ANDROID_SDK_ROOT="$HOME/android-sdk"
CMDLINE_VERSION=11076708
CMDLINE_ZIP="commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
CMDLINE_URL="https://dl.google.com/android/repository/${CMDLINE_ZIP}"

mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
cd "$ANDROID_SDK_ROOT"
if [[ ! -d cmdline-tools/latest/bin ]]; then
  echo "Downloading Android cmdline tools..."
  rm -f "$CMDLINE_ZIP"
  curl -L "$CMDLINE_URL" -o "$CMDLINE_ZIP"
  rm -rf cmdline-tools/latest
  unzip -qo "$CMDLINE_ZIP" -d cmdline-tools
  mv cmdline-tools/cmdline-tools cmdline-tools/latest
fi

export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"

# 3) Accept licenses and install build targets
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses <<EOF_LICENSES
y
y
y
y
y
y
y
y
EOF_LICENSES
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 4) Persist environment
PROFILE_LINE="export ANDROID_SDK_ROOT=\"$ANDROID_SDK_ROOT\""
if ! grep -qs "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT" "$HOME/.profile"; then
  {
    echo "$PROFILE_LINE"
    echo "export JAVA_HOME=\"$JAVA_HOME\""
    echo "export PATH=\"$JAVA_HOME/bin:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:\$PATH\""
  } >> "$HOME/.profile"
fi

# 5) Build the APK
cd "$HOME/Chain-analysis-news-publisher"
if [[ $BUILD_APP -eq 1 ]]; then
  ./gradlew --no-daemon assembleDebug
fi

echo "Setup complete. APK will be under app/build/outputs/apk/debug/."
