import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.github.ben-manes.versions")
}

val applicationName = "OrientationFaker"
val versionMajor = 4
val versionMinor = 6
val versionPatch = 2

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "net.mm2d.android.orientationfaker"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        vectorDrawables.useSupportLibrary = true
        base.archivesBaseName = "${applicationName}-${versionName}"
        multiDexEnabled = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "${applicationName}-${versionName}.apk"
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.browser:browser:1.2.0")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.lifecycle:lifecycle-process:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.webkit:webkit:1.3.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("com.google.android.play:core:1.8.3")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation("androidx.room:room-runtime:2.2.5")
    implementation("androidx.room:room-ktx:2.2.5")
    kapt("androidx.room:room-compiler:2.2.5")
    implementation("net.mm2d:log:0.9.3")
    implementation("net.mm2d:log-android:0.9.3")
    implementation("net.mm2d:color-chooser:0.2.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.5")
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.toUpperCase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) }
}
