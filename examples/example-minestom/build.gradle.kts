plugins {
    id("conventions.example")
    alias(libs.plugins.shadow)
}

indra {
    javaVersions {
        minimumToolchain(25)
        target(25)
        testWith().set(setOf(25))
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        manifest {
            attributes("Main-Class" to "org.incendo.cloud.examples.minestom.ExampleServer")
        }
    }
}

dependencies {
    implementation(project(":cloud-minestom"))
    implementation(libs.cloud.annotations)
    implementation(project(":cloud-minecraft-extras"))
    implementation(libs.minestom)
}
