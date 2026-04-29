plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    compileOnly(libs.minestom)
}

indra {
    javaVersions {
        minimumToolchain(25)
        target(25)
        testWith().set(setOf(25))
    }
}
