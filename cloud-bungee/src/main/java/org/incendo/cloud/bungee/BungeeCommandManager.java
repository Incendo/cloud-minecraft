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
package org.incendo.cloud.bungee;

import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.bungee.parser.PlayerParser;
import org.incendo.cloud.bungee.parser.ServerParser;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.execution.ExecutionCoordinator;

public class BungeeCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSender, C> {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    /**
     * Default caption for {@link BungeeCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'<input>' is not a valid player";

    /**
     * Default caption for {@link BungeeCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'<input>' is not a valid server";

    private final Plugin owningPlugin;
    private final SenderMapper<CommandSender, C> senderMapper;

    /**
     * Construct a new Bungee command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param senderMapper                 Function that maps {@link CommandSender} to the command sender type
     */
    @SuppressWarnings("unchecked")
    public BungeeCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new BungeePluginRegistrationHandler<>());
        ((BungeePluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.senderMapper = senderMapper;

        /* Register Bungee Preprocessor */
        this.registerCommandPreProcessor(new BungeeCommandPreprocessor<>(this));

        /* Register Bungee Parsers */
        this.parserRegistry()
                .registerParser(PlayerParser.playerParser())
                .registerParser(ServerParser.serverParser());

        /* Register default captions */
        this.captionRegistry()
            .registerProvider(CaptionProvider.<C>constantProvider()
                .putCaption(BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, ARGUMENT_PARSE_FAILURE_PLAYER)
                .putCaption(BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER, ARGUMENT_PARSE_FAILURE_SERVER)
                .build());

        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender,
            final @NonNull String permission
    ) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    /**
     * Returns the owning plugin.
     *
     * @return owning plugin
     */
    public @NonNull Plugin owningPlugin() {
        return this.owningPlugin;
    }

    private void registerDefaultExceptionHandlers() {
        this.exceptionController().registerHandler(Throwable.class, context -> {
            this.senderMapper.reverse(context.context().sender())
                    .sendMessage(new ComponentBuilder(MESSAGE_INTERNAL_ERROR).color(ChatColor.RED).create());
            this.owningPlugin.getLogger().log(
                    Level.SEVERE,
                    "An unhandled exception was thrown during command execution",
                    context.exception());
        }).registerHandler(CommandExecutionException.class, context -> {
            this.senderMapper.reverse(context.context().sender())
                    .sendMessage(new ComponentBuilder(MESSAGE_INTERNAL_ERROR)
                    .color(ChatColor.RED)
                    .create());
            this.owningPlugin.getLogger().log(
                    Level.SEVERE,
                    "Exception executing command handler",
                    context.exception().getCause()
            );
        }).registerHandler(ArgumentParseException.class, context -> this.senderMapper.reverse(
                context.context().sender()).sendMessage(new ComponentBuilder("Invalid Command Argument: ")
                .color(ChatColor.GRAY).append(context.exception().getCause().getMessage()).create())
        ).registerHandler(NoSuchCommandException.class, context -> this.senderMapper.reverse(
                context.context().sender()).sendMessage(new ComponentBuilder(MESSAGE_UNKNOWN_COMMAND)
                .color(ChatColor.WHITE).create())
        ).registerHandler(NoPermissionException.class, context -> this.senderMapper.reverse(
                context.context().sender()).sendMessage(new ComponentBuilder(MESSAGE_NO_PERMS)
                .color(ChatColor.WHITE).create())
        ).registerHandler(InvalidCommandSenderException.class, context -> this.senderMapper.reverse(
                context.context().sender()).sendMessage(new ComponentBuilder(context.exception().getMessage())
                .color(ChatColor.RED).create())
        ).registerHandler(InvalidSyntaxException.class, context -> this.senderMapper.reverse(
                context.context().sender()).sendMessage(new ComponentBuilder(
                        "Invalid Command Syntax. Correct command syntax is: ").color(ChatColor.RED).append("/")
                        .color(ChatColor.GRAY).append(context.exception().correctSyntax()).color(ChatColor.GRAY).create()
        ));
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }
}