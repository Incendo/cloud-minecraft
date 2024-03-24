plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    /* Needs to be provided by the platform */
    compileOnly(libs.brigadier)
    testImplementation(libs.brigadier)
}
