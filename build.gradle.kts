buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.1")
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    }
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
