plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.46" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.10" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
