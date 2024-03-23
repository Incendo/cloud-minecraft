plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    api(projects.cloudBrigadier)

    // We expect these to either be
    //   1) Provided by the platform at runtime
    //     or
    //   2) Shaded/included by the user either explicitly or as a
    //      transitive dependency of adventure-platform
    fun compileOnlyApiAndTests(dep: Any) {
        compileOnlyApi(dep)
        testImplementation(dep)
    }
    compileOnlyApiAndTests(libs.adventureApi)
    compileOnlyApiAndTests(libs.adventureTextSerializerPlain)
    // Only needed for features that explicitly mention MiniMessage
    compileOnlyApiAndTests(libs.minimessage)
}
