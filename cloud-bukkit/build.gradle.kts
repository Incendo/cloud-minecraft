plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    api(projects.cloudBrigadier)
    compileOnly(libs.bukkit)
    compileOnly(libs.commodore)
    testImplementation(libs.bukkit)
    javadocLinks(libs.paperApi) {
        isTransitive = false
    }
}

spotless {
    java {
        targetExclude(file("src/main/java/cloud/commandframework/bukkit/internal/MinecraftArgumentTypes.java"))
    }
}
