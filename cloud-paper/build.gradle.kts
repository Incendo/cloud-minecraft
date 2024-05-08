plugins {
    id("conventions.base")
    id("conventions.publishing")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    api(projects.cloudBukkit)
    compileOnly(libs.paperApi)
    compileOnly(libs.paperMojangApi)
    // TODO
    // javadocLinks(libs.paperApi) {
    //     isTransitive = false
    // }
}
