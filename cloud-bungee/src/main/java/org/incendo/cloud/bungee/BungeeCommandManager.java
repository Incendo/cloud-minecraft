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
import org.incendo.cloud.execution.ExecutionCoordinator;

public class BungeeCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSender, C> {

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
        this.registerDefaultExceptionHandlers(
            triplet -> {
                final CommandSender commandSender = this.senderMapper.reverse(triplet.first().sender());
                final String message = triplet.first().formatCaption(triplet.second(), triplet.third());
                commandSender.sendMessage(new ComponentBuilder(message).color(ChatColor.RED).create());
            },
            pair -> this.owningPlugin.getLogger().log(Level.SEVERE, pair.first(), pair.second())
        );
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }
}
