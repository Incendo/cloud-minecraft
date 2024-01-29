import org.incendo.cloudbuildlogic.JavadocLinksExtension

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

javadocLinks.filter = JavadocLinksExtension.DependencyFilter.NoSnapshots(
    exceptFor = setOf(
        "io.papermc.paper:paper-api"
    )
)
