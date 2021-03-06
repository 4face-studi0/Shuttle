plugins {
    id("shuttle.kotlin")
}

moduleDependencies {

    apps.domain()
    coordinates.domain()
    stats.domain()
}

dependencies {

    implementation(libs.bundles.base)

    implementation(libs.klock)

    testImplementation(libs.bundles.test.kotlin)
}
