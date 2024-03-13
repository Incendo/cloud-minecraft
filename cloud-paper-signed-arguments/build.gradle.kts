plugins {
    id("conventions.base")
    id("conventions.publishing")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    compileOnly(libs.paperApi)
    api(projects.cloudBukkit)
    implementation(libs.reflectionRemapper)
    api(projects.cloudMinecraftSignedArguments)
}
