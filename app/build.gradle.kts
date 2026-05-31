import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Read keystore credentials from local.properties (gitignored). If the file or
// any required property is missing (CI, fresh checkout), the release build runs
// unsigned and `bundleRelease` still completes — it just produces an unsigned
// artifact that can't be installed until signed.
val releaseSigningProps: Properties? = rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.let { f -> Properties().apply { f.inputStream().use { load(it) } } }
    ?.takeIf {
        // Require both path + password before activating release signing.
        // Missing either → fall through to an unsigned release build instead of
        // failing at sign time.
        it.getProperty("RELEASE_KEYSTORE_PATH")?.isNotBlank() == true &&
            it.getProperty("RELEASE_KEYSTORE_PASSWORD")?.isNotBlank() == true
    }

android {
    namespace = "com.perpenda"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.perpenda"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"https://aware-wholeness-production-d771.up.railway.app/\"")
        // Sentry DSN is a public identifier (it ships in every APK by design;
        // Sentry rate-limits unknown clients). Safe to commit.
        buildConfigField(
            "String",
            "SENTRY_DSN",
            "\"https://b4b6960ba5c8f8a940cdb35e8f297ccd@o4511485646405632.ingest.de.sentry.io/4511485699620944\""
        )
        // Gate for the in-app sideload-update banner. Default OFF — Google Play's
        // Device and Network Abuse policy forbids Play-distributed apps from
        // prompting users to install APKs outside Play. The `release` buildType
        // below opts in because today's release artifact is the sideload APK
        // hosted on GitHub Releases. Any future Play build (separate flavor or
        // buildType) MUST leave this `false`.
        buildConfigField("boolean", "SIDELOAD_DISTRIBUTION", "false")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (releaseSigningProps != null) {
            create("release") {
                storeFile = file(releaseSigningProps.getProperty("RELEASE_KEYSTORE_PATH"))
                storePassword = releaseSigningProps.getProperty("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = releaseSigningProps.getProperty("RELEASE_KEY_ALIAS") ?: "perpenda-upload"
                keyPassword = releaseSigningProps.getProperty("RELEASE_KEY_PASSWORD")
                    ?: releaseSigningProps.getProperty("RELEASE_KEYSTORE_PASSWORD")
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
            signingConfig = signingConfigs.findByName("release")
            // Today's release artifact is the sideload APK on GitHub Releases.
            // Switch this to `false` (or remove this line) when a Play-targeted
            // build is introduced.
            buildConfigField("boolean", "SIDELOAD_DISTRIBUTION", "true")
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.3")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("io.sentry:sentry-android:7.21.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
