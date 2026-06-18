import java.util.Properties

// Uniquement sous Linux/WSL : corrige local.properties si Android Studio a écrit un chemin Windows.
fun fixLocalPropertiesForWsl() {
    val isLinux = System.getProperty("os.name").lowercase().contains("linux")
    if (!isLinux) return

    val wslSdk = "${System.getProperty("user.home")}/Android/Sdk"
    if (!file(wslSdk).isDirectory) return

    val localPropertiesFile = file("local.properties")
    val props = Properties()
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { props.load(it) }
    }

    val currentSdk = props.getProperty("sdk.dir").orEmpty()
    val looksLikeWindowsPath = currentSdk.contains(':')
    val currentExists = currentSdk.isNotBlank() && file(currentSdk).isDirectory

    if (looksLikeWindowsPath || !currentExists) {
        props.setProperty("sdk.dir", wslSdk)
        localPropertiesFile.outputStream().use {
            props.store(it, "Auto-fixed for WSL: Gradle requires Linux SDK path")
        }
    }
}

fixLocalPropertiesForWsl()

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "BlindfoldChessTrainer"
include(":app")
include(":core:chess")