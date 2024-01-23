plugins {
    id("conventions.base")
    id("conventions.publishing")
}

dependencies {
    api(libs.cloud.core)
    api(libs.adventureApi)
    api(libs.adventureTextSerializerPlain)
    compileOnlyApi(libs.minimessage)
}
