plugins {
    id("conventions.example")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.waterfall)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    runWaterfall {
        waterfallVersion("1.19")
    }
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-bungee"))
    implementation(libs.cloud.annotations)
    implementation(project(":cloud-minecraft-extras"))
    /* Extras */
    implementation(libs.adventurePlatformBungeecord)
    /* Bungee*/
    compileOnly(libs.bungeecord)
}
