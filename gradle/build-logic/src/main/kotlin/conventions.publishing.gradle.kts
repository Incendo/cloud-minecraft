import org.incendo.cloudbuildlogic.city
import org.incendo.cloudbuildlogic.jmp

plugins {
    id("org.incendo.cloud-build-logic.publishing")
}

if (!name.endsWith("-bom")) {
    dependencies {
        JavaPlugin.API_CONFIGURATION_NAME(platform(project(":cloud-minecraft-bom")))
    }
}

indra {
    github("Incendo", "cloud-minecraft") {
        ci(true)
    }
    mitLicense()

    configurePublications {
        pom {
            developers {
                jmp()
                city()
            }
        }
    }
}

javadocLinks {
    defaultJavadocProvider = "https://www.javadocs.dev/{group}/{name}/{version}"
}

publishing {
    val user: String? = (project.findProperty("spectrisUsername") ?: System.getenv("spectrisUsername")) as? String
    val pass: String? = (project.findProperty("spectrisPassword") ?: System.getenv("spectrisPassword")) as? String

    repositories {
        maven {
            name = "spectris-snapshots"
            url = uri("https://repo.spectr.is/snapshots/")
            credentials {
                username = user
                password = pass
            }
        }

        maven {
            name = "spectris-releases"
            url = uri("https://repo.spectr.is/releases/")
            credentials {
                username = user
                password = pass
            }
        }
    }
}
