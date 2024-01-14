import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.incendo.cloud-build-logic")
    id("org.incendo.cloud-build-logic.spotless")
    id("org.incendo.cloud-build-logic.errorprone")
}

indra {
    checkstyle().set(libs.versions.checkstyle)
}

spotless {
    java {
        importOrderFile(rootProject.file(".spotless/cloud.importorder"))
    }
}

cloudSpotless {
    ktlintVersion.set(libs.versions.ktlint)
}

dependencies {
    checkstyle(libs.stylecheck)
    errorprone(libs.errorproneCore)
    compileOnly(libs.bundles.immutables)
    annotationProcessor(libs.bundles.immutables)
    testImplementation(libs.bundles.baseTestingDependencies)
}

/* Disable checkstyle on tests */
gradle.startParameter.excludedTaskNames.add("checkstyleTest")
