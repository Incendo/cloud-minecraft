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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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
    private final Map<String, Set<String>> aliases = new ConcurrentHashMap<>();
    private final Set<Command<C>> registeredCommands = new HashSet<>();
    private volatile @Nullable Commands commands;

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
        this.commands = commands;

        this.aliases.clear();
        for (final CommandNode<C> rootNode : this.manager.commandTree().rootNodes()) {
            this.registerCommand(commands, rootNode);
        }
    }

    private void registerCommand(final Commands commands, final CommandNode<C> rootNode) {
        final Set<String> registered = commands.registerWithFlags(
            this.manager.owningPlugin().getPluginMeta(),
            this.createRootNode(rootNode, rootNode.component().name()),
            this.findBukkitDescription(rootNode),
            new ArrayList<>(rootNode.component().alternativeAliases()),
            new HashSet<>(Collections.singletonList(CommandRegistrationFlag.FLATTEN_ALIASES))
        );
        this.aliases.put(rootNode.component().name(), registered);
    }

    private LiteralCommandNode<CommandSourceStack> createRootNode(final CommandNode<C> rootNode, final String label) {
        final BrigadierPermissionChecker<C> permissionChecker = (sender, permission) -> {
            // We need to check that the command still exists...
            if (this.manager.commandTree().getNamedNode(rootNode.component().name()) == null) {
                return false;
            }

            return this.manager.testPermission(sender, permission).allowed();
        };
        return this.brigadierManager.literalBrigadierNodeFactory().createNode(
            label,
            rootNode,
            new CloudBrigadierCommand<>(
                this.manager,
                this.brigadierManager,
                command -> BukkitHelper.stripNamespace(this.manager, command)
            ),
            permissionChecker
        );
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
        if (!this.registeredCommands.add(command)) {
            return true;
        }
        final @Nullable Commands commands = this.commands;
        if (commands == null) {
            return true;
        }

        if (this.aliases.containsKey(command.rootComponent().name())) {
            final CommandDispatcher<CommandSourceStack> dispatcher =
                unsafeGet(commands, Commands::getDispatcher);
            final Set<String> registered = this.aliases.get(command.rootComponent().name());
            final LiteralCommandNode<CommandSourceStack> newRoot = this.createRootNode(
                this.manager.commandTree().getNamedNode(command.rootComponent().name()),
                command.rootComponent().name()
            );
            for (final String label : registered) {
                final com.mojang.brigadier.tree.CommandNode<CommandSourceStack> node =
                    dispatcher.getRoot().getChild(label);
                for (final com.mojang.brigadier.tree.CommandNode<CommandSourceStack> newChild : newRoot.getChildren()) {
                    node.addChild(newChild);
                }
            }
        } else {
            unsafeOperation(commands, cmds -> this.registerCommand(
                cmds,
                this.manager.commandTree().getNamedNode(command.rootComponent().name())
            ));
        }

        this.resendCommands();

        final @Nullable Set<String> registered = this.aliases.get(command.rootComponent().name());

        boolean ret = registered != null && !registered.isEmpty();
        if (!ret) {
            this.registeredCommands.remove(command);
        }
        return ret;
    }

    private static @MonotonicNonNull Method COMMANDNODE_REMOVE_METHOD = null;

    private void unregisterRoot(final Commands commands, final String label) {
        final @Nullable Set<String> removed = this.aliases.remove(label);
        if (removed == null || removed.isEmpty()) {
            return;
        }
        this.registeredCommands.removeIf(command -> command.rootComponent().name().equals(label));

        try {
            if (COMMANDNODE_REMOVE_METHOD == null) {
                COMMANDNODE_REMOVE_METHOD = com.mojang.brigadier.tree.CommandNode.class.getMethod(
                    "removeCommand", String.class
                );
                COMMANDNODE_REMOVE_METHOD.setAccessible(true);
            }
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException("Failed to find removeCommand method", e);
        }

        unsafeOperation(commands, cmds -> {
            final CommandDispatcher<CommandSourceStack> dispatcher = cmds.getDispatcher();
            final RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
            for (final String removedLabel : removed) {
                try {
                    COMMANDNODE_REMOVE_METHOD.invoke(root, removedLabel);
                } catch (final ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to delete node " + removedLabel, e);
                }
            }
        });
    }

    @Override
    public void unregisterRootCommand(final @NonNull CommandComponent<C> rootCommand) {
        final @Nullable Commands commands = this.commands;
        if (commands == null) {
            return;
        }

        this.unregisterRoot(commands, rootCommand.name());

        this.resendCommands();
    }

    private void resendCommands() {
        for (final Player player : this.manager.owningPlugin().getServer().getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    private static @MonotonicNonNull Field COMMANDS_INVALID_FIELD = null;

    private static void unsafeOperation(final Commands commands, final Consumer<Commands> task) {
        unsafeGet(commands, cmds -> {
            task.accept(cmds);
            return null;
        });
    }

    private static <T> T unsafeGet(final Commands commands, final Function<Commands, T> task) {
        try {
            if (COMMANDS_INVALID_FIELD == null) {
                COMMANDS_INVALID_FIELD = commands.getClass().getDeclaredField("invalid");
                COMMANDS_INVALID_FIELD.setAccessible(true);
            }
            final boolean prev = COMMANDS_INVALID_FIELD.getBoolean(commands);
            try {
                COMMANDS_INVALID_FIELD.setBoolean(commands, false);
                return task.apply(commands);
            } finally {
                COMMANDS_INVALID_FIELD.setBoolean(commands, prev);
            }
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException("Failed to perform unsafe command operation", e);
        }
    }
}
