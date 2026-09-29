plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    compileOnly(libs.waterdog) {
        isTransitive = false
    }
}
