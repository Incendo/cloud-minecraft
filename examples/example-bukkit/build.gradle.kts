import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("conventions.example")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-paper"))
    implementation(libs.cloud.annotations)
    implementation(project(":cloud-minecraft-extras"))
    implementation(projects.cloudPaperSignedArguments)
    /* Extras */
    implementation(libs.adventurePlatformBukkit)
    implementation(libs.minimessage)
    /* Bukkit */
    compileOnly(libs.bukkit)
    /* Annotation processing */
    annotationProcessor(libs.cloud.annotations)
}

tasks {
    shadowJar {
        // adventure-platform
        relocate("net.kyori", "org.incendo.cloud.example.kyori")

        // cloud
        // relocate("org.incendo.cloud", "my.package.cloud") // We don't relocate cloud itself in this example, but you still should

        // cloud dependency
        relocate("io.leangen.geantyref", "org.incendo.cloud.example.geantyref")

        // cloud-paper dependencies
        relocate("xyz.jpenilla.reflectionremapper", "org.incendo.cloud.example.reflectionremapper")
        relocate("net.fabricmc.mappingio", "org.incendo.cloud.example.mappingio")

        mergeServiceFiles()

        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
    assemble {
        dependsOn(shadowJar)
    }

    val runVersions = mapOf(
        8 to setOf("1.8.8"),
        11 to setOf("1.9.4", "1.10.2", "1.11.2"),
        17 to setOf("1.12.2", "1.13.2", "1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.20.4"),
        21 to setOf("1.20.6", "1.21.7"),
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
