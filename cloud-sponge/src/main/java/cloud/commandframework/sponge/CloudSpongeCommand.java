//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.sponge;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.LiteralParser;
import cloud.commandframework.arguments.compound.CompoundParser;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.types.tuples.Pair;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

final class CloudSpongeCommand<C> implements Command.Raw {

    private static final Component NULL = text("null");
    private static final Component MESSAGE_INTERNAL_ERROR =
        text("An internal error occurred while attempting to perform this command.", RED);
    private static final Component MESSAGE_NO_PERMS =
        text("I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.", RED);
    private static final Component MESSAGE_UNKNOWN_COMMAND = text("Unknown command. Type \"/help\" for help.");

    private final SpongeCommandManager<C> commandManager;
    private final String label;

    CloudSpongeCommand(
        final @NonNull String label,
        final @NonNull SpongeCommandManager<C> commandManager
    ) {
        this.label = label;
        this.commandManager = commandManager;
    }

    @Override
    public CommandResult process(final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable arguments) {
        final C cloudSender = this.commandManager.senderMapper().map(cause);
        final String input = this.formatCommandForParsing(arguments.input());
        this.commandManager.commandExecutor().executeCommand(cloudSender, input);
        return CommandResult.success();
    }

    // todo
    public static <C> void registerExceptionHandlers(final SpongeCommandManager<C> mgr) {
        mgr.exceptionController().registerHandler(InvalidSyntaxException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(text().append(
                text("Invalid Command Syntax. Correct command syntax is: ", RED),
                text("/" + ctx.exception().correctSyntax(), GRAY)
            ).build());
        });
        mgr.exceptionController().registerHandler(InvalidCommandSenderException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(text(ctx.exception().getMessage(), RED));
        });
        mgr.exceptionController().registerHandler(NoPermissionException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_NO_PERMS);
        });
        mgr.exceptionController().registerHandler(NoSuchCommandException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_UNKNOWN_COMMAND);
        });
        mgr.exceptionController().registerHandler(ArgumentParseException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(text().append(
                text("Invalid Command Argument: ", RED),
                getMessage(ctx.exception().getCause()).colorIfAbsent(GRAY)
            ).build());
        });
        mgr.exceptionController().registerHandler(CommandExecutionException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_INTERNAL_ERROR);
            mgr.owningPluginContainer().logger()
                .error("Exception executing command handler", ctx.exception().getCause());
        });
        mgr.exceptionController().registerHandler(Throwable.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_INTERNAL_ERROR);
            mgr.owningPluginContainer().logger()
                .error("An unhandled exception was thrown during command execution", ctx.exception());
        });
    }

    private static Component getMessage(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

    @Override
    public List<CommandCompletion> complete(
        final @NonNull CommandCause cause,
        final ArgumentReader.@NonNull Mutable arguments
    ) {
        return this.commandManager.suggestionFactory().suggestImmediately(
            this.commandManager.senderMapper().map(cause),
            this.formatCommandForSuggestions(arguments.input())
            // todo
        ).list().stream().map(s -> CommandCompletion.of(s.suggestion())).collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(final @NonNull CommandCause cause) {
        return this.commandManager.testPermission(
            this.commandManager.senderMapper().map(cause),
            (Permission) this.namedNode().nodeMeta().getOrDefault(CommandNode.META_KEY_PERMISSION, Permission.empty())
        ).allowed();
    }

    @Override
    public Optional<Component> shortDescription(final CommandCause cause) {
        return Optional.of(this.usage(cause));
    }

    @Override
    public Optional<Component> extendedDescription(final CommandCause cause) {
        return Optional.of(this.usage(cause));
    }

    @Override
    public Optional<Component> help(final @NonNull CommandCause cause) {
        return Optional.of(this.usage(cause));
    }

    @Override
    public Component usage(final CommandCause cause) {
        return text(this.commandManager.commandSyntaxFormatter().apply(this.commandManager.senderMapper().map(cause), Collections.emptyList(), this.namedNode()));
    }

    private CommandNode<C> namedNode() {
        return this.commandManager.commandTree().getNamedNode(this.label);
    }

    @Override
    public CommandTreeNode.Root commandTree() {
        final CommandTreeNode<CommandTreeNode.Root> root = CommandTreeNode.root();

        final CommandNode<C> cloud = this.namedNode();

        if (cloud.isLeaf() || cloud.command() != null) {
            root.executable();
        }

        this.addRequirement(cloud, root);

        this.addChildren(root, cloud);
        return (CommandTreeNode.Root) root;
    }

    private void addChildren(final CommandTreeNode<?> node, final CommandNode<C> cloud) {
        for (final CommandNode<C> child : cloud.children()) {
            final CommandComponent<C> value = child.component();
            final CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>> treeNode;
            if (value.parser() instanceof LiteralParser) {
                treeNode = (CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>) CommandTreeNode.literal();
            } else if (value.parser() instanceof CompoundParser) {
                final CompoundParser<?, C, ?> compound = (CompoundParser<?, C, ?>) value.parser();
                this.handleCompoundArgument(node, child, compound);
                continue;
            } else {
                treeNode = this.commandManager.parserMapper().mapComponent(value);
            }
            this.addRequirement(child, treeNode);
            if (canExecute(child)) {
                treeNode.executable();
            }
            this.addChildren(treeNode, child);
            node.child(value.name(), treeNode);
        }
    }

    private void handleCompoundArgument(
        final CommandTreeNode<?> node,
        final CommandNode<C> child,
        final CompoundParser<?, C, ?> compound
    ) {
        final CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>> treeNode;
        final ArrayDeque<Pair<String, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>>> nodes = new ArrayDeque<>();
        for (final CommandComponent<C> component : compound.components()) {
            final String name = component.name();
            nodes.add(Pair.of(name, this.commandManager.parserMapper().mapParser(component.parser())));
        }
        Pair<String, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>> argument = null;
        while (!nodes.isEmpty()) {
            final Pair<String, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>> prev = argument;
            argument = nodes.removeLast();
            if (prev != null) {
                argument.second().child(prev.first(), prev.second());
            } else {
                // last node
                if (canExecute(child)) {
                    argument.second().executable();
                }
            }
            this.addRequirement(child, argument.second());
        }
        treeNode = argument.second();
        this.addChildren(treeNode, child);
        node.child(compound.components().get(0).toString(), treeNode);
    }

    private static <C> boolean canExecute(final @NonNull CommandNode<C> node) {
        return node.isLeaf()
            || !node.component().required()
            || node.command() != null
            || node.children().stream().noneMatch(c -> c.component().required());
    }

    private void addRequirement(
        final @NonNull CommandNode<C> cloud,
        final @NonNull CommandTreeNode<? extends CommandTreeNode<?>> node
    ) {
        final Permission permission = (Permission) cloud.nodeMeta()
            .getOrDefault(CommandNode.META_KEY_PERMISSION, Permission.empty());
        if (permission == Permission.empty()) {
            return;
        }
        node.requires(cause ->
            this.commandManager.testPermission(this.commandManager.senderMapper().map(cause), permission).allowed());
    }

    private String formatCommandForParsing(final @NonNull String arguments) {
        if (arguments.isEmpty()) {
            return this.label;
        }
        return this.label + " " + arguments;
    }

    private String formatCommandForSuggestions(final @NonNull String arguments) {
        return this.label + " " + arguments;
    }

}
