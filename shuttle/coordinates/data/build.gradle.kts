plugins {
    id("shuttle.android")
}

moduleDependencies {

    coordinates.domain()
    database()
}

dependencies {

    implementation(libs.bundles.base)

    implementation(libs.androidx.workManager)
    implementation(libs.geohash)
    implementation(libs.klock)
    implementation(libs.koin.android)
    implementation(libs.koin.android.workManager)
    implementation(libs.playServices.location)

    testImplementation(libs.bundles.test.kotlin)
    androidTestImplementation(libs.bundles.test.android)
    androidTestImplementation(libs.androidx.workManager.test)
}
