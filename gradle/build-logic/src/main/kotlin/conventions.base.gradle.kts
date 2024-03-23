import gradle.kotlin.dsl.accessors._c15003335100b45636a8dae476bde48e.javadocLinks
import org.incendo.cloudbuildlogic.util.ciBuild

plugins {
    id("org.incendo.cloud-build-logic")
    id("org.incendo.cloud-build-logic.spotless")
    id("org.incendo.cloud-build-logic.errorprone")
    id("org.incendo.cloud-build-logic.javadoc-links")
}

indra {
    checkstyle().set(libs.versions.checkstyle)
}

spotless {
    java {
        importOrderFile(rootProject.file(".spotless/cloud.importorder"))
    }
}

javadocLinks {
    excludes.add("com.mojang:brigadier")
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
    testImplementation(libs.bundles.baseTestingDependencies)
}

/* Disable checkstyle on tests */
gradle.startParameter.excludedTaskNames.add("checkstyleTest")
