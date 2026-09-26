plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.quickqr.scanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quickqr.scanner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Load signing from env vars (CI) or keystore.properties (local). Never commit secrets.
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = java.util.Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    fun signingProp(envName: String, fileKey: String): String? =
        System.getenv(envName)?.takeIf { it.isNotBlank() }
            ?: keystoreProperties.getProperty(envName)?.takeIf { it.isNotBlank() }
            ?: keystoreProperties.getProperty(fileKey)?.takeIf { it.isNotBlank() }

    val storeFilePath = signingProp("STORE_FILE", "storeFile")
    val storePasswordValue = signingProp("STORE_PASSWORD", "storePassword")
    val keyAliasValue = signingProp("KEY_ALIAS", "keyAlias")
    val keyPasswordValue = signingProp("KEY_PASSWORD", "keyPassword")
    val hasReleaseSigning =
        storeFilePath != null &&
            storePasswordValue != null &&
            keyAliasValue != null &&
            keyPasswordValue != null

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                // STORE_FILE may be absolute or relative to the project root
                storeFile = rootProject.file(storeFilePath!!)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
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
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Without signing props, assembleRelease still works but produces an unsigned APK.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.accompanist.permissions)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
