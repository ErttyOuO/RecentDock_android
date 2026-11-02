#!/usr/bin/env sh
set -eu
GRADLE_VERSION="9.3.1"
GRADLE_SHA256="b266d5ff6b90eada6dc3b20cb090e3731302e553a27c5d3e4df1f0d76beaff06"
BOOT_DIR="${HOME}/.gradle/recentdock-bootstrap"
GRADLE_HOME="${BOOT_DIR}/gradle-${GRADLE_VERSION}"
ZIP_PATH="${BOOT_DIR}/gradle-${GRADLE_VERSION}-bin.zip"

if [ ! -x "${GRADLE_HOME}/bin/gradle" ]; then
  mkdir -p "${BOOT_DIR}"
  echo "[RecentDock] Downloading Gradle ${GRADLE_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "${ZIP_PATH}"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "${ZIP_PATH}" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  else
    echo "curl or wget is required for first-time Gradle bootstrap." >&2
    exit 1
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    echo "${GRADLE_SHA256}  ${ZIP_PATH}" | sha256sum -c -
  fi
  unzip -q -o "${ZIP_PATH}" -d "${BOOT_DIR}"
fi

exec "${GRADLE_HOME}/bin/gradle" "$@"
