plugins {
    id("conventions.base")
}

indra {
    javaVersions {
        target(17)
        minimumToolchain(21)
        testWith().set(setOf(17, 21))
    }
}
