import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.openrndr.android"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":openrndr-application"))
    implementation(project(":openrndr-draw"))
    implementation(project(":openrndr-shape"))
    implementation(project(":openrndr-binpack"))
    implementation(project(":openrndr-dds"))
    implementation(project(":openrndr-extensions"))
    implementation(project(":openrndr-gl-common"))
    implementation(libs.kotlin.coroutines)
    implementation(libs.lwjgl.opengles)
}
