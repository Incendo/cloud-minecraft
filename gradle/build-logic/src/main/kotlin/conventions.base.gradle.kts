import org.incendo.cloudbuildlogic.util.ciBuild

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

if (providers.ciBuild.get() && libs.versions.cloudCore.get().endsWith("-SNAPSHOT")) {
    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(1, TimeUnit.MINUTES)
        }
    }
}

dependencies {
    checkstyle(libs.stylecheck)
    errorprone(libs.errorproneCore)
    compileOnly(libs.bundles.immutables)
    annotationProcessor(libs.bundles.immutables)

    testImplementation(libs.jupiterEngine)
    testImplementation(libs.jupiterParams)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.mockitoJupiter)
    testImplementation(libs.truth)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/* Disable checkstyle on tests */
gradle.startParameter.excludedTaskNames.add("checkstyleTest")
