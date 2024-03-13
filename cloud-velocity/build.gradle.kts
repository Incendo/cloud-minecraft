plugins {
    id("conventions.base")
    id("conventions.publishing")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    api(libs.cloud.core)
    api(projects.cloudBrigadier)
    compileOnly(libs.velocityApi)
}
