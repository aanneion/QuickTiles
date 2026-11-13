plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    namespace = "com.flxholle.quicktiles"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.asdoi.quicktiles"
        minSdk = 24
        targetSdk = 36
        versionCode = 29
        versionName = "2.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The app is translated into these languages; the support libraries ship translations for
        // every locale there is, and that table is by far the biggest thing in the apk. Dropping the
        // locales the app does not speak saves around 600 KiB - for those users the library strings
        // fall back to English instead.
        resourceConfigurations += listOf("en", "de", "tr", "zh-rTW")
    }

    signingConfigs {
        // The CI generates this keystore once and caches it between runs, so every QuickTiles
        // build - debug and release - carries the same signature and installs over the previous
        // version without uninstalling it first. It is a throw-away keystore for personal builds,
        // not something to publish to an app store with.
        create("ci") {
            val ciKeystore = rootProject.file(".ci/debug.keystore")
            if (ciKeystore.exists()) {
                storeFile = ciKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            // Shrinking is safe here: every tile service is declared in the manifest, every
            // preference class is either referenced from code or covered by app/proguard-rules.pro.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Falls back to the local debug keystore when the CI keystore is not present, so a
            // release build always produces an installable APK.
            signingConfig = if (rootProject.file(".ci/debug.keystore").exists()) {
                signingConfigs.getByName("ci")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    dependenciesInfo {
        // Play metadata inside the apk, pointless for builds that are installed by hand
        includeInApk = false
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/*.version",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.view)
    implementation(libs.searchpreference)

    implementation(libs.androidx.preference.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}