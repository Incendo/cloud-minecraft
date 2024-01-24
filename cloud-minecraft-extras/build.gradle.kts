plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)

    // We expect these to either be
    //   1) Provided by the platform at runtime
    //     or
    //   2) Shaded/included by the user either explicitly or as a
    //      transitive dependency of adventure-platform
    compileOnlyApi(libs.adventureApi)
    compileOnlyApi(libs.adventureTextSerializerPlain)
    // Only needed for features that explicitly mention MiniMessage
    compileOnlyApi(libs.minimessage)
}
