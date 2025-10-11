import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("conventions.example")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

indra {
    javaVersions {
        target(21)
    }
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-paper"))
    implementation(libs.cloud.annotations)
    implementation(project(":cloud-minecraft-extras"))
    implementation(projects.cloudPaperSignedArguments)
    /* Bukkit */
    compileOnly(libs.paperApi)
    /* Annotation processing */
    annotationProcessor(libs.cloud.annotations)
}

tasks {
    shadowJar {
        // cloud
        // relocate("org.incendo.cloud", "my.package.cloud") // We don't relocate cloud itself in this example, but you still should

        // cloud dependency
        relocate("io.leangen.geantyref", "org.incendo.cloud.example.geantyref")

        // cloud-paper dependencies
        relocate("xyz.jpenilla.reflectionremapper", "org.incendo.cloud.example.reflectionremapper")
        relocate("net.fabricmc.mappingio", "org.incendo.cloud.example.mappingio")

        mergeServiceFiles()
        // Needed for mergeServiceFiles to work properly in Shadow 9+
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
    assemble {
        dependsOn(shadowJar)
    }

    val runVersions = mapOf(
        21 to setOf("1.20.6", "1.21.1", "1.21.5", "1.21.10"),
    )

    runServer {
        minecraftVersion(runVersions[runVersions.maxOf { it.key }]!!.last())
        runDirectory(file("run/latest"))
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(runVersions.maxOf { it.key }))
            }
        )
    }

    // Set up a run task for each supported version
    runVersions.forEach { (javaVersion, minecraftVersions) ->
        for (version in minecraftVersions) {
            createVersionedRun(version, javaVersion)
        }
    }
}

fun TaskContainerScope.createVersionedRun(
    version: String,
    javaVersion: Int
) = register<RunServer>("runServer${version.replace(".", "_")}") {
    group = "cloud"
    pluginJars.from(shadowJar.flatMap { it.archiveFile })
    minecraftVersion(version)
    runDirectory(file("run/$version"))
    systemProperty("Paper.IgnoreJavaVersion", true)
    javaLauncher.set(
        project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    )
}
