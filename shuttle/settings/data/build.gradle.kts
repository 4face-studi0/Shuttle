plugins {
    id("shuttle.android")
}

moduleDependencies {

    apps.domain()
    database()
    settings.domain()
}

dependencies {

    implementation(libs.bundles.base)
    implementation(libs.koin.android)
    implementation(libs.androidx.dataStore.preferences)

    testImplementation(libs.bundles.test.kotlin)
    androidTestImplementation(libs.bundles.test.android)
}
