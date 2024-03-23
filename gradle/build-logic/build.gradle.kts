plugins {
    `kotlin-dsl`
    alias(libs.plugins.cloud.buildLogic.spotless)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatypeOssSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    implementation(libs.cloud.build.logic)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

cloudSpotless {
    licenseHeaderFile.convention(null as RegularFile?)
    ktlintVersion = libs.versions.ktlint
}
