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

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.incendo.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.CommandRegistrationHandler;

@SuppressWarnings("UnstableApiUsage")
final class ModernPaperBrigadier<C> implements CommandRegistrationHandler<C>, BrigadierManagerHolder<C, CommandSourceStack> {
    private final PaperCommandManager<C> manager;
    private final CloudBrigadierManager<C, CommandSourceStack> brigadierManager;

    ModernPaperBrigadier(final PaperCommandManager<C> manager) {
        this.manager = manager;
        this.brigadierManager = new CloudBrigadierManager<>(
            this.manager,
            () -> new CommandContext<>(
                this.manager.senderMapper().map(Bukkit.getConsoleSender()),
                this.manager
            ),
            SenderMapper.create(
                sender -> this.manager.senderMapper().map(sender.getSender()),
                new BukkitBackwardsBrigadierSenderMapper<>(this.manager)
            )
        );

        final BukkitBrigadierMapper<C> mapper =
            new BukkitBrigadierMapper<>(this.manager, this.brigadierManager);
        mapper.registerBuiltInMappings();
        PaperBrigadierMappings.register(mapper);

        // TODO - Allow registering in bootstrap/onEnable per-root-note, based on meta value
        manager.owningPlugin().getLifecycleManager()
            .registerEventHandler(LifecycleEvents.COMMANDS, this::register);
    }

    private void register(final ReloadableRegistrarEvent<Commands> event) {
        this.manager.lockRegistration0(); // Lock registration once event is called

        final Commands commands = event.registrar();

        for (final CommandNode<C> rootNode : this.manager.commandTree().rootNodes()) {
            final BrigadierPermissionChecker<C> permissionChecker = (sender, permission) -> {
                // We need to check that the command still exists...
                if (this.manager.commandTree().getNamedNode(rootNode.component().name()) == null) {
                    return false;
                }

                return this.manager.testPermission(sender, permission).allowed();
            };
            final LiteralCommandNode<CommandSourceStack> brigNode = this.brigadierManager.literalBrigadierNodeFactory().createNode(
                rootNode.component().name(),
                rootNode,
                new CloudBrigadierCommand<>(
                    this.manager,
                    this.brigadierManager,
                    command -> BukkitHelper.stripNamespace(this.manager, command)
                ),
                permissionChecker
            );
            commands.registerWithFlags(
                this.manager.owningPlugin().getPluginMeta(),
                brigNode,
                this.findBukkitDescription(rootNode),
                new ArrayList<>(rootNode.component().alternativeAliases()),
                new HashSet<>(Collections.singletonList(CommandRegistrationFlag.FLATTEN_ALIASES))
            );
        }
    }

    private String findBukkitDescription(final CommandNode<C> node) {
        if (node.command() != null) {
            return BukkitHelper.description(node.command());
        }
        for (final CommandNode<C> child : node.children()) {
            final @Nullable String result = this.findBukkitDescription(child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public boolean hasBrigadierManager() {
        return true;
    }

    @Override
    public @NonNull CloudBrigadierManager<C, CommandSourceStack> brigadierManager() {
        return this.brigadierManager;
    }

    @Override
    public boolean registerCommand(final @NonNull Command<C> command) {
        return true;
    }

    @Override
    public void unregisterRootCommand(final @NonNull CommandComponent<C> rootCommand) {
    }
}
