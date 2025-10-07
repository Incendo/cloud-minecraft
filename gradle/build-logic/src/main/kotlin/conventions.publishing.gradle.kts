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
