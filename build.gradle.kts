plugins {
    id("shuttle.detekt")
    id("shuttle.modulesCatalog")
    id("com.google.devtools.ksp") version "1.7.0-1.0.6"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle.android)
        classpath(libs.gradle.crashlytics)
        classpath(libs.gradle.gsm)
        classpath(libs.gradle.kotlin)
        classpath(libs.gradle.sqlDelight)
    }
}

apply(from = "gradle/applyProjectRepositories.gradle.kts")
