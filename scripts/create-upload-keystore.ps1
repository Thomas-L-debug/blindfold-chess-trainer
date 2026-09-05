# Creates a Play upload keystore (once) and writes gitignored keystore.properties.
# Back up keystore/upload-keystore.jks and keystore.properties offline. Losing them
# blocks Play updates until Google resets the upload key.
param(
    [string]$ProjectDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $jbr = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path $jbr) { $javaHome = $jbr }
}
if (-not $javaHome) {
    Write-Error "JAVA_HOME is not set and Android Studio JBR was not found."
}

$keytool = Join-Path $javaHome "bin\keytool.exe"
if (-not (Test-Path $keytool)) {
    Write-Error "keytool not found at $keytool"
}

$keystoreDir = Join-Path $ProjectDir "keystore"
$keystorePath = Join-Path $keystoreDir "upload-keystore.jks"
$propsPath = Join-Path $ProjectDir "keystore.properties"

if (Test-Path $keystorePath) {
    Write-Host "Keystore already exists: $keystorePath"
    if (-not (Test-Path $propsPath)) {
        Write-Error "keystore.properties is missing. Restore it from backup; do not generate a second key."
    }
    Write-Host "keystore.properties is present. Nothing to do."
    exit 0
}

New-Item -ItemType Directory -Force -Path $keystoreDir | Out-Null

$bytes = New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$password = ([Convert]::ToBase64String($bytes) -replace "[^A-Za-z0-9]", "").Substring(0, 24)

& $keytool -genkeypair -v `
    -keystore $keystorePath `
    -storetype JKS `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000 `
    -alias upload `
    -storepass $password `
    -keypass $password `
    -dname "CN=Blindfold Chess Trainer, O=Blindfold Chess Trainer, C=FR"

if ($LASTEXITCODE -ne 0) {
    Write-Error "keytool failed with exit code $LASTEXITCODE"
}

@"
storeFile=keystore/upload-keystore.jks
storePassword=$password
keyAlias=upload
keyPassword=$password
"@ | Set-Content -Path $propsPath -Encoding ASCII

Write-Host "Created $keystorePath"
Write-Host "Wrote $propsPath (gitignored)."
Write-Host "Back up both files now. Do not commit them."
