plugins {
    id("conventions.base")
}

indra {
    javaVersions {
        target(17)
        minimumToolchain(17)
        testWith().set(setOf(17))
    }
}
