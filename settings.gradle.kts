enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatypeOssSnapshots"
            mavenContent { snapshotsOnly() }
        }
    }
    includeBuild("gradle/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatypeOssSnapshots"
            mavenContent { snapshotsOnly() }
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatypeS01OssSnapshots"
            mavenContent { snapshotsOnly() }
        }
        /* The Minecraft repository, used for cloud-brigadier */
        maven("https://libraries.minecraft.net/") {
            name = "minecraftLibraries"
            mavenContent {
                releasesOnly()
                includeGroup("com.mojang")
                includeGroup("net.minecraft")
            }
        }
        /* The paper repository, used for cloud-paper */
        maven("https://repo.papermc.io/repository/maven-public/")
        /* Used for cloud-cloudburst */
        maven("https://repo.opencollab.dev/maven-snapshots/") {
            name = "cloudburst"
            mavenContent {
                snapshotsOnly()
                includeGroup("org.cloudburstmc")
            }
        }
        /* The current Sponge repository */
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
            mavenContent { includeGroup("org.spongepowered") }
        }
    }
}

rootProject.name = "cloud-minecraft"

include("cloud-minecraft-bom")

include("cloud-brigadier")
include("cloud-bukkit")
include("cloud-bungee")
include("cloud-cloudburst")
include("cloud-minecraft-extras")
include("cloud-minecraft-signed-arguments")
include("cloud-paper")
include("cloud-paper-signed-arguments")
include("cloud-sponge7")
include("cloud-velocity")

include("examples/example-bukkit")
findProject(":examples/example-bukkit")?.name = "example-bukkit"

include("examples/example-paper")
findProject(":examples/example-paper")?.name = "example-paper"

include("examples/example-bungee")
findProject(":examples/example-bungee")?.name = "example-bungee"

include("examples/example-velocity")
findProject(":examples/example-velocity")?.name = "example-velocity"
