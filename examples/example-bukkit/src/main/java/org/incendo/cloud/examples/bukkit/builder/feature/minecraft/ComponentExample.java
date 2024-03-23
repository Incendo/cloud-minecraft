package org.incendo.cloud.examples.bukkit.builder.feature.minecraft;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;
import org.incendo.cloud.minecraft.extras.parser.ComponentParser;

/**
 * Example showcasing the component parser from cloud-minecraft-extras.
 */
public final class ComponentExample implements BuilderFeature {

    @Override
    public void registerFeature(
        final @NonNull ExamplePlugin examplePlugin,
        final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
            manager.commandBuilder("builder")
                .literal("minimessage")
                .required("msg", ComponentParser.miniMessageParser())
                .handler(c -> {
                    examplePlugin.bukkitAudiences().sender(c.sender()).sendMessage(c.get("msg"));
                })
        );
    }
}
