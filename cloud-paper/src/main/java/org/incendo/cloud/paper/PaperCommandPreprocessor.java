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
package org.incendo.cloud.paper;

import java.util.concurrent.Executor;
import java.util.function.Function;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;

@DefaultQualifier(NonNull.class)
final class PaperCommandPreprocessor<B, C> implements CommandPreprocessor<C> {

    private static final boolean FOLIA =
            CraftBukkitReflection.classExists("io.papermc.paper.threadedregions.RegionizedServer");

    private final PluginHolder pluginHolder;
    private final SenderMapper<B, C> mapper;
    private final Function<B, CommandSender> senderExtractor;

    PaperCommandPreprocessor(
        final PluginHolder pluginHolder,
        final SenderMapper<B, C> mapper,
        final Function<B, CommandSender> senderExtractor
    ) {
        this.pluginHolder = pluginHolder;
        this.mapper = mapper;
        this.senderExtractor = senderExtractor;
    }

    @Override
    public void accept(final CommandPreprocessingContext<C> ctx) {
        // When we have a BukkitCommandManager,
        // it's preprocessor will store the main thread executor if we don't store anything.
        if (FOLIA) {
            ctx.commandContext().store(
                    BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR,
                    this.foliaExecutorFor(ctx.commandContext().sender())
            );
        } else if (!(this.pluginHolder instanceof BukkitCommandManager)) {
            ctx.commandContext().store(
                BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR,
                BukkitHelper.mainThreadExecutor(this.pluginHolder)
            );
        }
    }

    private Executor foliaExecutorFor(final C sender) {
        final CommandSender commandSender = this.senderExtractor.apply(this.mapper.reverse(sender));
        final Plugin plugin = this.pluginHolder.owningPlugin();
        if (commandSender instanceof Entity) {
            return task -> {
                ((Entity) commandSender).getScheduler().run(
                        plugin,
                        handle -> task.run(),
                        null
                );
            };
        } else if (commandSender instanceof BlockCommandSender) {
            final BlockCommandSender blockSender = (BlockCommandSender) commandSender;
            return task -> {
                blockSender.getServer().getRegionScheduler().run(
                        plugin,
                        blockSender.getBlock().getLocation(),
                        handle -> task.run()
                );
            };
        }
        return task -> {
            plugin.getServer().getGlobalRegionScheduler().run(
                    plugin,
                    handle -> task.run()
            );
        };
    }
}
