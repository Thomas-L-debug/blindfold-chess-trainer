# Configuration Android Studio pour le projet sur disque Windows (ex. D:\CodingProject\...).
param(
    [string]$ProjectDir = "D:\CodingProject\blindfold-chess-trainer",
    [string]$SdkDir = "$env:LOCALAPPDATA\Android\Sdk"
)

$gradleXml = Join-Path $ProjectDir ".idea\gradle.xml"
$localProps = Join-Path $ProjectDir "local.properties"

if (-not (Test-Path $ProjectDir)) {
    Write-Error "Projet introuvable : $ProjectDir"
    exit 1
}

# local.properties
$escapedSdk = $SdkDir -replace '\\', '\\'
@"
## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
sdk.dir=$escapedSdk
"@ | Set-Content -Path $localProps -Encoding UTF8

# Gradle JDK = Android Studio (pas WSL)
if (Test-Path $gradleXml) {
    (Get-Content $gradleXml -Raw) `
        -replace '<option name="gradleJvm" value="21 \(WSL\)" />', '<option name="gradleJvm" value="jbr-21" />' `
        | Set-Content -Path $gradleXml -NoNewline
}

Write-Host "OK"
Write-Host "  local.properties -> $SdkDir"
Write-Host "  gradleJvm -> jbr-21"
Write-Host ""
Write-Host "Dans Android Studio : File -> Sync Project with Gradle Files"