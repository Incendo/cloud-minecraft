plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    compileOnly(projects.cloudBrigadier)
    compileOnly(libs.brigadier)

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
    compileOnly(libs.adventureTextSerializerLegacy) // for ComponentParser annotation mapping
    compileOnly(libs.adventureTextSerializerGson) // for ComponentParser annotation mapping
    // Only needed for features that explicitly mention MiniMessage
    compileOnlyApiAndTests(libs.minimessage)
}
