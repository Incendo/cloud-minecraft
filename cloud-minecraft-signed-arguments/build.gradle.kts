plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    compileOnly(libs.brigadier)
    compileOnlyApi(libs.adventureApi)
    api(projects.cloudBrigadier)
}
