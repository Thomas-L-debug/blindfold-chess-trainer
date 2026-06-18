#!/usr/bin/env bash
# Force le SDK Linux pour Gradle WSL (Android Studio réécrit souvent le chemin Windows).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS="$ROOT/local.properties"
WSL_SDK="$HOME/Android/Sdk"

if [[ ! -d "$WSL_SDK" ]]; then
  echo "SDK WSL introuvable : $WSL_SDK"
  echo "Installe-le avec sdkmanager ou lance d'abord le setup Android dans Ubuntu."
  exit 1
fi

cat > "$PROPS" <<EOF
## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
# Gradle (WSL) lit ce chemin Linux. Android Studio (Windows) doit utiliser
# \\\\wsl.localhost\\Ubuntu\\home\\thomas\\Android\\Sdk dans Settings → Android SDK.
sdk.dir=$WSL_SDK
EOF

echo "OK → sdk.dir=$WSL_SDK (pour Gradle/WSL)"
echo ""
echo "Dans Android Studio (Windows), Settings → Android SDK → Android SDK Location :"
echo "  \\\\wsl.localhost\\Ubuntu\\home\\thomas\\Android\\Sdk"
echo ""
echo "NE PAS mettre /home/thomas/... dans Android Studio (chemin Linux invisible sous Windows)."
echo "Relance le sync dans Android Studio."