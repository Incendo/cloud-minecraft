package org.incendo.cloud.examples.bukkit.builder.feature.minecraft;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;

import static org.incendo.cloud.bukkit.parser.EnchantmentParser.enchantmentParser;

public class EnchantmentExample implements BuilderFeature {

    @Override
    public void registerFeature(
        final @NonNull ExamplePlugin examplePlugin,
        final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
            manager.commandBuilder("builder")
                .literal("enchantment")
                .required("enchant", enchantmentParser())
                .handler(ctx -> {
                    final Enchantment enchantment = ctx.get("enchant");
                    ctx.sender().sendMessage("The enchant you typed is '" + enchantment.getKey() + "'.");
                })
        );
    }
}
