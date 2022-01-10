buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    }
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
