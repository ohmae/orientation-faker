buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0")
        classpath(kotlin("gradle-plugin", version = "1.6.21"))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    }
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
