import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// ---------------------------------------------------------------------------
// Release signing — read ONLY from environment variables. Never falls back to
// the debug key: a release artifact without these variables fails the build.
// ---------------------------------------------------------------------------
fun envOrNull(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

val releaseKeystorePath: String? = envOrNull("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword: String? = envOrNull("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = envOrNull("ANDROID_KEY_ALIAS")
val releaseKeyPassword: String? = envOrNull("ANDROID_KEY_PASSWORD")

val hasReleaseSigning: Boolean = releaseKeystorePath != null &&
    releaseKeystorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

val missingSigningMessage = """
    |
    |Release signing is NOT configured — refusing to build a release artifact.
    |Set all four environment variables and run the task again:
    |  ANDROID_KEYSTORE_PATH      absolute path to the PKCS12 (.p12) keystore
    |  ANDROID_KEYSTORE_PASSWORD  keystore password
    |  ANDROID_KEY_ALIAS          key alias inside the keystore
    |  ANDROID_KEY_PASSWORD       key password (same as keystore password for PKCS12)
    |Debug signing is never used for release builds. See README → "Local signing".
    """.trimMargin()

val releaseTaskRequested: Boolean = gradle.startParameter.taskNames.any { requested ->
    val task = requested.substringAfterLast(':').lowercase()
    val isCheckOnly = task.startsWith("test") || task.contains("unittest") ||
        task.contains("androidtest") || task.startsWith("lint") || task == "clean"
    !isCheckOnly && (task.contains("release") || task == "build" || task == "assemble" || task == "bundle")
}

if (releaseTaskRequested) {
    if (!hasReleaseSigning) throw GradleException(missingSigningMessage)
    val keystore = file(releaseKeystorePath!!)
    if (!keystore.isFile) {
        throw GradleException("ANDROID_KEYSTORE_PATH points to a missing file: ${keystore.absolutePath}")
    }
}

android {
    namespace = "com.jokinojewels.game"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.jokinojewels.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                storeType = "PKCS12"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            // Staged R8: ship the first release non-minified. Enable both flags only
            // after a verified non-minified release (see README → "Staged R8").
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // No native code in this app; keeps the package 16 KB page-size friendly.
            useLegacyPackaging = false
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Safety net: even if a release task is reached through an abbreviated or IDE task
// name, packaging/signing a release artifact without the env vars still fails.
tasks.configureEach {
    if (name == "packageRelease" || name == "signReleaseBundle") {
        doFirst {
            if (!hasReleaseSigning) throw GradleException(missingSigningMessage)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.common.ktx)

    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.datastore.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
