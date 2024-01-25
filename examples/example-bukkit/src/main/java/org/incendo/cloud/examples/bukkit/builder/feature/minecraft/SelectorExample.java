//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.incendo.cloud.examples.bukkit.builder.feature.minecraft;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.data.SingleEntitySelector;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;

import static org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser.singleEntitySelectorParser;

/**
 * Example showcasing how to work with entity selectors.
 */
public final class SelectorExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(manager.commandBuilder("builder")
                .literal("teleport")
                .literal("entity")
                .required("entity", singleEntitySelectorParser(), Description.of("Entity to teleport"))
                .literal("here")
                .senderType(Player.class)
                .handler(commandContext -> {
                    final Player player = commandContext.sender();
                    final SingleEntitySelector singleEntitySelector = commandContext.get("entity");
                    player.getServer().getScheduler().runTask(examplePlugin, () -> {
                        singleEntitySelector.single().teleport(player);
                    });
                    player.sendMessage(ChatColor.GREEN + "The entity was teleported to you!");
                }));
    }
}
