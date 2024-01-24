plugins {
    alias(libs.plugins.shadow)
    id("conventions.example")
    alias(libs.plugins.run.velocity)
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("com.velocitypowered:velocity-api"))
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    runVelocity {
        velocityVersion(libs.versions.velocityApi.get())
    }
}

dependencies {
    api(project(":cloud-velocity"))
    api(project(":cloud-minecraft-extras"))
    api(libs.cloud.annotations)
    annotationProcessor(libs.velocityApi)
    compileOnly(libs.velocityApi)
}
