import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing comes from keystore.properties (local builds) or from RELEASE_* environment
// variables (CI). -PunsignedRelease builds an unsigned release APK, used by the PR workflow to
// exercise R8 without access to the key.
val unsignedRelease = providers.gradleProperty("unsignedRelease").isPresent
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun signingValue(property: String, env: String): String? =
    keystoreProperties.getProperty(property) ?: providers.environmentVariable(env).orNull
val releaseStoreFile = signingValue("storeFile", "RELEASE_KEYSTORE_PATH")
val releaseStorePassword = signingValue("storePassword", "RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
val hasReleaseSigning = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    .all { !it.isNullOrBlank() }

android {
    namespace = "io.github.perroabuelo.materialeleven"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "io.github.perroabuelo.materialeleven"
        minSdk = 26
        targetSdk = 36
        versionCode = 100
        versionName = "0.1.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigning && !unsignedRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        // Lists the app's languages for the per-app language setting of Android 13+.
        generateLocaleConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}

val checkReleaseSigning = tasks.register("checkReleaseSigning") {
    val canBuild = hasReleaseSigning || unsignedRelease
    doLast {
        if (!canBuild) {
            throw GradleException(
                "Release signing is not configured. Create keystore.properties (see README) or set " +
                    "RELEASE_KEYSTORE_PATH, RELEASE_KEYSTORE_PASSWORD, RELEASE_KEY_ALIAS and " +
                    "RELEASE_KEY_PASSWORD. Use -PunsignedRelease for an unsigned build."
            )
        }
    }
}

tasks.configureEach {
    if (name == "packageRelease") dependsOn(checkReleaseSigning)
}
