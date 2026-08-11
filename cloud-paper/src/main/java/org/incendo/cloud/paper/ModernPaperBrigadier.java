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
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
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
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.CommandRegistrationHandler;

@SuppressWarnings("UnstableApiUsage")
final class ModernPaperBrigadier<C, B> implements CommandRegistrationHandler<C>, BrigadierManagerHolder<C, CommandSourceStack> {
    private final CommandManager<C> manager;
    private final Runnable lockRegistration;
    private final PluginMetaHolder metaHolder;
    private final CloudBrigadierManager<C, CommandSourceStack> brigadierManager;
    private final Map<String, Set<String>> aliases = new ConcurrentHashMap<>();
    private final Set<Command<C>> registeredCommands = new HashSet<>();
    private volatile @Nullable Commands commands;

    // TODO - Allow registering in bootstrap/onEnable per-root-note, based on meta value?
    @SuppressWarnings("unchecked")
    ModernPaperBrigadier(
        final Class<B> baseType,
        final CommandManager<C> manager,
        final SenderMapper<B, C> senderMapper,
        final Runnable lockRegistration
    ) {
        this.manager = manager;
        this.lockRegistration = lockRegistration;

        if (manager instanceof PluginMetaHolder) {
            this.metaHolder = (PluginMetaHolder) manager;
        } else if (manager instanceof PluginHolder) {
            this.metaHolder = PluginMetaHolder.fromPluginHolder((PluginHolder) manager);
        } else {
            throw new IllegalArgumentException(manager.toString());
        }

        this.brigadierManager = new CloudBrigadierManager<>(
            this.manager,
            SenderMapper.create(
                source -> {
                    if (baseType.equals(CommandSender.class)) {
                        return senderMapper.map((B) source.getSender());
                    } else {
                        return senderMapper.map((B) source);
                    }
                },
                sender -> {
                    if (baseType.equals(CommandSender.class)) {
                        return (CommandSourceStack) new BukkitBackwardsBrigadierSenderMapper<>(senderMapper).apply(sender);
                    } else {
                        return (CommandSourceStack) senderMapper.reverse(sender);
                    }
                }
            )
        );

        final BukkitBrigadierMapper<C> mapper =
            new BukkitBrigadierMapper<>(Logger.getLogger(this.metaHolder.owningPluginMeta().getName()), this.brigadierManager);
        mapper.registerBuiltInMappings();
        PaperBrigadierMappings.register(mapper);
    }

    void registerPlugin(final Plugin plugin) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::register);
    }

    void registerBootstrap(final BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::register);
    }

    private void register(final ReloadableRegistrarEvent<Commands> event) {
        this.lockRegistration.run(); // Lock registration once event is called

        final Commands commands = event.registrar();
        this.commands = commands;

        this.aliases.clear();
        for (final CommandNode<C> rootNode : this.manager.commandTree().rootNodes()) {
            this.registerRoot(commands, rootNode);
        }
    }

    private void registerRoot(final Commands commands, final CommandNode<C> rootNode) {
        final String rootName = rootNode.component().name();
        final Set<String> registered = commands.registerWithFlags(
            this.metaHolder.owningPluginMeta(),
            this.createRootNode(rootNode, rootName),
            this.findBukkitDescription(rootNode),
            new ArrayList<>(rootNode.component().alternativeAliases()),
            new HashSet<>(Collections.singletonList(CommandRegistrationFlag.FLATTEN_ALIASES))
        );
        this.aliases.put(rootName, registered);
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
                command -> BukkitHelper.stripNamespace(this.metaHolder.owningPluginMeta().getName(), command)
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

        try {
            this.syncRoot(commands, command.rootComponent().name());
        } catch (final RuntimeException e) {
            try {
                this.resendCommands();
            } catch (final RuntimeException resendFailure) {
                e.addSuppressed(resendFailure);
            }
            throw e;
        }

        this.resendCommands();
        return true;
    }

    /**
     * Rebuilds the root literal that {@code rootName} resolves to and replaces its registration.
     *
     * <p>The nodes handed out by the API dispatcher are mirrors of the server nodes rather than the server nodes
     * themselves, so adding children to a node fetched from the dispatcher does not change what the server dispatches or
     * sends to clients. The entire root has to be unregistered and registered again for a change to take effect.</p>
     *
     * <p>If the registration fails the root is left unregistered, so every command belonging to it is dropped from
     * {@link #registeredCommands} to make a later insert able to restore it.</p>
     *
     * @param commands the registrar to operate on
     * @param rootName a name or alias of the root literal
     */
    private void syncRoot(final Commands commands, final String rootName) {
        final @Nullable CommandNode<C> rootNode = this.manager.commandTree().getNamedNode(rootName);
        if (rootNode == null) {
            this.forgetRoot(rootName);
            return;
        }

        final String resolvedName = rootNode.component().name();
        try {
            unsafeOperation(commands, cmds -> {
                this.removeRootLabels(cmds, resolvedName);
                this.registerRoot(cmds, rootNode);
            });
        } catch (final RuntimeException e) {
            this.forgetRoot(resolvedName);
            throw e;
        }
    }

    /**
     * Drops every command belonging to the root literal named {@code rootName} from {@link #registeredCommands}.
     *
     * @param rootName the primary name of the root literal
     */
    private void forgetRoot(final String rootName) {
        this.registeredCommands.removeIf(registered -> registered.rootComponent().name().equals(rootName));
    }

    private static @MonotonicNonNull Method commandNodeRemoveMethod = null;

    private static @NonNull Method commandNodeRemoveMethod() {
        if (commandNodeRemoveMethod == null) {
            try {
                commandNodeRemoveMethod = com.mojang.brigadier.tree.CommandNode.class.getMethod(
                    "removeCommand", String.class
                );
                commandNodeRemoveMethod.setAccessible(true);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Failed to find removeCommand method", e);
            }
        }
        return commandNodeRemoveMethod;
    }

    /**
     * Removes every label that the root literal named {@code rootName} is currently registered under.
     *
     * <p>Must be called from within {@link #unsafeOperation(Commands, Consumer)}.</p>
     *
     * @param commands the registrar to operate on
     * @param rootName the primary name of the root literal
     */
    private void removeRootLabels(final Commands commands, final String rootName) {
        final @Nullable Set<String> removed = this.aliases.remove(rootName);
        if (removed == null) {
            return;
        }

        final RootCommandNode<CommandSourceStack> root = commands.getDispatcher().getRoot();
        for (final String label : removed) {
            try {
                commandNodeRemoveMethod().invoke(root, label);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Failed to delete node " + label, e);
            }
        }
    }

    @Override
    public void unregisterRootCommand(final @NonNull CommandComponent<C> rootCommand) {
        final String rootName = rootCommand.name();
        this.forgetRoot(rootName);

        final @Nullable Commands commands = this.commands;
        if (commands == null) {
            return;
        }

        unsafeOperation(commands, cmds -> this.removeRootLabels(cmds, rootName));
        this.resendCommands();
    }

    private void resendCommands() {
        for (final Player player : this.metaHolder.owningPlugin().getServer().getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    private static @MonotonicNonNull Field commandsInvalidField = null;

    private static void unsafeOperation(final Commands commands, final Consumer<Commands> task) {
        try {
            if (commandsInvalidField == null) {
                commandsInvalidField = commands.getClass().getDeclaredField("invalid");
                commandsInvalidField.setAccessible(true);
            }
            final boolean prev = commandsInvalidField.getBoolean(commands);
            try {
                commandsInvalidField.setBoolean(commands, false);
                task.accept(commands);
            } finally {
                commandsInvalidField.setBoolean(commands, prev);
            }
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException("Failed to perform unsafe command operation", e);
        }
    }
}
