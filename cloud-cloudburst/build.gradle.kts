plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    compileOnly(libs.cloudburst) {
        isTransitive = false
    }
    api(projects.cloudMinecraftSignedArguments)
}
