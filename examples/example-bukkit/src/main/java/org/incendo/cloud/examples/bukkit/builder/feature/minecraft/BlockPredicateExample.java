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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.data.BlockPredicate;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;

import static org.incendo.cloud.bukkit.parser.BlockPredicateParser.blockPredicateParser;
import static org.incendo.cloud.bukkit.parser.MaterialParser.materialParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

/**
 * Example showcasing the block predicate parser.
 */
public final class BlockPredicateExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(manager.commandBuilder("builder")
                .literal("mc113")
                .literal("replace")
                .senderType(Player.class)
                .required("predicate", blockPredicateParser())
                .literal("with")
                .required("block", materialParser()) // todo: use BlockDataArgument
                .required("radius", integerParser(1 /* minValue */))
                .handler(this::executeReplace));
    }

    private void executeReplace(final CommandContext<Player> ctx) {
        final BlockData block = ctx.<Material>get("block").createBlockData();
        final BlockPredicate predicate = ctx.get("predicate");
        final int radius = ctx.get("radius");

        final Player player = ctx.sender();
        final Location loc = player.getLocation();

        for (double x = loc.getX() - radius; x < loc.getX() + radius; x++) {
            for (double y = loc.getY() - radius; y < loc.getY() + radius; y++) {
                for (double z = loc.getZ() - radius; z < loc.getZ() + radius; z++) {
                    final Block blockAt = player.getWorld().getBlockAt((int) x, (int) y, (int) z);
                    if (predicate.test(blockAt)) {
                        blockAt.setBlockData(block);
                    }
                }
            }
        }
    }
}
