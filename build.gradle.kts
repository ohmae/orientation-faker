plugins {
    id("com.android.application") version "8.0.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.3" apply false
    id("dagger.hilt.android.plugin") version "2.45" apply false
    id("com.google.devtools.ksp") version "1.8.20-1.0.10" apply false
    id("com.github.ben-manes.versions") version "0.46.0" apply false

    // for release
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
