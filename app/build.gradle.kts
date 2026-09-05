import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.isFile) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

fun releaseStoreFile(): java.io.File? {
    val relative = keystoreProperties.getProperty("storeFile") ?: return null
    val file = rootProject.file(relative)
    return file.takeIf { it.isFile }
}

android {
    namespace = "com.blindfoldchess.trainer"
    compileSdk = 36

    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "com.blindfoldchess.trainer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON",
                )
            }
        }
    }

    signingConfigs {
        val store = releaseStoreFile()
        if (store != null) {
            create("release") {
                storeFile = store
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                    ?: keystoreProperties.getProperty("storePassword")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:chess"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

val pageSize16Kb = 16384L

tasks.register("checkReleasePageSize") {
    group = "verification"
    description = "Fails if release native libraries are not 16 KB ELF-aligned"
    dependsOn("mergeReleaseNativeLibs")
    doLast {
        val libRoot = layout.buildDirectory
            .dir("intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib")
            .get()
            .asFile
        val soFiles = libRoot.walkTopDown().filter { it.isFile && it.extension == "so" }.toList()
        check(soFiles.isNotEmpty()) {
            "No release .so files under $libRoot — native merge did not run"
        }
        soFiles.forEach { so ->
            val aligns = elfLoadAlignments(so)
            check(aligns.isNotEmpty()) { "${so.name} has no PT_LOAD segments" }
            val minAlign = aligns.min()
            check(minAlign >= pageSize16Kb) {
                "${so.relativeTo(libRoot)} LOAD align is $minAlign (need >= $pageSize16Kb)"
            }
            logger.lifecycle("OK 16 KB: ${so.relativeTo(libRoot)} min LOAD align=$minAlign")
        }
    }
}

tasks.matching { it.name == "bundleRelease" }.configureEach {
    finalizedBy("checkReleasePageSize")
}

fun elfLoadAlignments(so: java.io.File): List<Long> {
    val bytes = so.readBytes()
    check(bytes.size >= 64) { "${so.name} is too small to be ELF" }
    check(
        bytes[0] == 0x7F.toByte() &&
            bytes[1] == 'E'.code.toByte() &&
            bytes[2] == 'L'.code.toByte() &&
            bytes[3] == 'F'.code.toByte(),
    ) { "${so.name} is not ELF" }
    val little = bytes[5] == 1.toByte()
    fun u16(off: Int): Int {
        val b0 = bytes[off].toInt() and 0xff
        val b1 = bytes[off + 1].toInt() and 0xff
        return if (little) b0 or (b1 shl 8) else (b0 shl 8) or b1
    }
    fun u32(off: Int): Long {
        var value = 0L
        if (little) {
            for (i in 0..3) value = value or ((bytes[off + i].toLong() and 0xff) shl (8 * i))
        } else {
            for (i in 0..3) value = (value shl 8) or (bytes[off + i].toLong() and 0xff)
        }
        return value
    }
    fun u64(off: Int): Long {
        var value = 0L
        if (little) {
            for (i in 0..7) value = value or ((bytes[off + i].toLong() and 0xff) shl (8 * i))
        } else {
            for (i in 0..7) value = (value shl 8) or (bytes[off + i].toLong() and 0xff)
        }
        return value
    }
    val is64 = bytes[4] == 2.toByte()
    val phoff = if (is64) u64(32) else u32(28)
    val phentsize = u16(if (is64) 54 else 42)
    val phnum = u16(if (is64) 56 else 44)
    val aligns = mutableListOf<Long>()
    repeat(phnum) { index ->
        val off = (phoff + index.toLong() * phentsize).toInt()
        val type = u32(off)
        if (type != 1L) return@repeat
        aligns += if (is64) u64(off + 48) else u32(off + 28)
    }
    return aligns
}
