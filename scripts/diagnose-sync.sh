#!/usr/bin/env bash
# Reproduit le sync Gradle d'Android Studio et affiche l'erreur complète.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "=== Environnement ==="
echo "PWD: $PWD"
echo "JAVA_HOME: ${JAVA_HOME:-<non défini>}"
java -version 2>&1 | head -1
echo ""

if [[ -f local.properties ]]; then
  echo "=== local.properties ==="
  grep sdk.dir local.properties || true
  SDK_PATH="$(grep -E '^sdk\.dir=' local.properties | cut -d= -f2- | sed 's/\\:/:/g; s/\\\\/\\/g')"
  echo "SDK résolu: $SDK_PATH"
  if [[ -d "$SDK_PATH" ]]; then
    echo "SDK: OK (dossier existe)"
  elif [[ -d "/mnt/c/Users/thoma/AppData/Local/Android/Sdk" ]]; then
    echo "SDK Windows accessible via /mnt/c/... mais pas via sdk.dir actuel"
  else
    echo "SDK: INTROUVABLE — cause fréquente de sync fail"
  fi
  echo ""
else
  echo "=== local.properties MANQUANT ==="
  echo ""
fi

echo "=== Gradle sync (assembleDebug) ==="
./gradlew :app:assembleDebug --no-daemon --stacktrace "$@"