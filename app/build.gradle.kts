plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "fr.upjv.lesombresduson"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.upjv.lesombresduson"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
    // App
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.play.services.auth)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.uiautomator)
    implementation(libs.lifecycle.common.jvm)
    implementation(libs.core.ktx)

    // Unit tests (local JVM)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)

    // Instrumented tests (device/emulator)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.mockito.android)
}
