plugins {
    id("conventions.base")
    id("conventions.publishing")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    compileOnly(libs.paperApi)
    api(projects.cloudPaper)
    implementation(libs.reflectionRemapper)
    api(projects.cloudMinecraftSignedArguments)
}
