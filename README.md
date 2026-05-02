<div align="center">
<img src="https://github.com/Incendo/cloud/raw/master/img/CloudNew.png" width="300px"/>
<br/>
<h1>cloud-minecraft</h1>

![license](https://img.shields.io/github/license/incendo/cloud.svg)
[![central](https://img.shields.io/maven-central/v/org.incendo/cloud-paper)](https://search.maven.org/search?q=org.incendo)
![build](https://img.shields.io/github/actions/workflow/status/incendo/cloud-minecraft/build.yml?logo=github)
[![docs](https://img.shields.io/readthedocs/incendocloud?logo=readthedocs)](https://cloud.incendo.org)
</div>

## This Fork
This is a quick fork of Incendo's cloud-minecraft repository that adds [Minestom](https://minestom.net/) support, as well as a few performance optimisations for platforms that do supertype checks.\
Everything is published at https://repo.spectr.is/.

### Setup
Add our repository and dependencies to your `build.gradle(.kts)`:

```kotlin
repositories {
    maven("https://repo.spectr.is/snapshots") {
        name = "spectris-snapshots"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    // 2.1.0-SNAPSHOT is the latest snapshot as of right now
    implementation("org.incendo:cloud-minestom:2.1.0-SNAPSHOT")
    
    // or, for the supertype caching optimisations:
    implementation("org.incendo:cloud-paper:2.1.0-SNAPSHOT") // or cloud-bungee or cloud-bukkit
}
```

### Usage (Minestom)

```java
final MinestomCommandManager<CommandSender> manager = new MinestomCommandManager<>(
    ExecutionCoordinator.simpleCoordinator(),
    SenderMapper.identity()
);

// Register whatever commands via the manager, or use the annotation parser (as instructed w/ https://cloud.incendo.org/core/)
```

See [examples/example-minestom](https://github.com/spectr-is/cloud-minecraft/blob/master/examples/example-minestom/src/main/java/org/incendo/cloud/examples/minestom/ExampleServer.java)

### Minestom Parsers
As of right now there's only two built-in parsers that I bothered to add.

## Included parsers

| Parser                | Type            | Description                                                                                         |
|-----------------------|-----------------|-----------------------------------------------------------------------------------------------------|
| `PlayerParser`        | `Player`        | Resolves an online player by username                                                               |
| `EntityTypeParser`    | `EntityType`    | Resolves an entity type by namespaced key                                                           |
| `InstanceParser`      | `Instance`      | Resolves a loaded instance by UUID                                                                  |
| `GameModeParser`      | `GameMode`      | Resolves a game mode by name                                                                        |
| `DimensionTypeParser` | `DimensionType` | Resolves a dimension type by namespaced key                                                         |
| `VecParser`           | `Vec`           | Resolves a `Vec` from `x y z` (supports relative coordinates (`~ ~ ~`)                              |
| `PosParser`           | `Pos`           | Resolves a `Pos` from `x y z`, (+ optional pitch/yaw - also supports relative coords (`~ ~ ~ ~ ~`)) |

## links

- JavaDoc: https://javadoc.io/doc/org.incendo
- Docs: https://cloud.incendo.org/minecraft
- Incendo Discord: https://discord.gg/aykZu32

## modules

- cloud-brigadier: integration with [Mojang Brigadier](https://github.com/Mojang/brigadier)
- cloud-paper: integration for Bukkit-based platforms with specific support for [Paper API](https://papermc.io/software/paper)
- cloud-bukkit: integration for Bukkit-based platforms, dependency of cloud-paper
- cloud-velocity: integration for [Velocity API](https://papermc.io/software/velocity)
- cloud-sponge7: integration for [Sponge API](https://spongepowered.org) v7
- cloud-bungee: integration for Bungeecord API
- cloud-cloudburst: integration for cloudburst
- cloud-minecraft-extras: optional extras using [adventure](https://github.com/KyoriPowered/adventure) API
- cloud-minecraft-bom: [bill of materials](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Importing_Dependencies) for cloud-minecraft dependencies
