plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    compileOnly(libs.brigadier)
    compileOnly(libs.adventureApi)
    compileOnly(projects.cloudBrigadier)
}
