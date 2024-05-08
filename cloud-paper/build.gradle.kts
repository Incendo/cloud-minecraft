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
    javadocLinks(libs.paperApi) {
        isTransitive = false
    }
}

configurations.javadocLinksSources {
    exclude("io.papermc.paper")
}
