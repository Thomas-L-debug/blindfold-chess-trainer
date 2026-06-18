#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Correction des droits sur les dossiers de build (souvent créés par Docker en root)…"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Ce script doit être lancé avec sudo :"
  echo "  sudo ./scripts/fix-build-permissions.sh"
  exit 1
fi

TARGET_USER="${SUDO_USER:-thomas}"
TARGET_GROUP="$(id -gn "${TARGET_USER}")"

chown -R "${TARGET_USER}:${TARGET_GROUP}" \
  "${ROOT}/app/build" \
  "${ROOT}/core/chess/build" \
  "${ROOT}/.gradle" 2>/dev/null || true

rm -rf \
  "${ROOT}/app/build" \
  "${ROOT}/core/chess/build"

echo "OK. Relance : ./gradlew test assembleDebug"