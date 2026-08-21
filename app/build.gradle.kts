import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Upload-key credentials for Play releases. Read from a gitignored
// keystore.properties, with environment variables taking precedence so CI can
// supply secrets without writing them to disk. Absent credentials are not an
// error: debug builds and unsigned assembleRelease keep working untouched.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(key: String, envVar: String): String? =
    System.getenv(envVar)?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }

val storeFilePath = signingValue("storeFile", "BYGRAMS_STORE_FILE")
val hasUploadKey = storeFilePath != null && rootProject.file(storeFilePath).exists()

android {
    namespace = "com.walnutgeek.bygrams"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.walnutgeek.bygrams"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.1"
    }

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = rootProject.file(storeFilePath!!)
                storePassword = signingValue("storePassword", "BYGRAMS_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "BYGRAMS_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "BYGRAMS_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Left unsigned when no upload key is configured, so a plain
            // assembleRelease still works for anyone building from source.
            signingConfig = if (hasUploadKey) signingConfigs.getByName("upload") else null
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.snakeyaml)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    debugImplementation(libs.androidx.ui.tooling)
}
