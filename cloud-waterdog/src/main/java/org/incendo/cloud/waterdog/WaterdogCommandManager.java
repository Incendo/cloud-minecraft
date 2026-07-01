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
package org.incendo.cloud.waterdog;

import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.waterdog.parser.PlayerParser;
import org.incendo.cloud.waterdog.parser.ServerParser;

/**
 * Command manager for the WaterdogPE platform
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public class WaterdogCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSender, C> {

    /**
     * Default caption for {@link WaterdogCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'<input>' is not a valid player";

    /**
     * Default caption for {@link WaterdogCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'<input>' is not a valid server";

    /**
     * WaterdogPE color code used to highlight error messages sent to command senders.
     */
    private static final String RED_COLOR_CODE = "§c";

    private final Plugin owningPlugin;
    private final SenderMapper<CommandSender, C> senderMapper;

    /**
     * Construct a new WaterdogPE command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param senderMapper                 Function that maps {@link CommandSender} to the command sender type
     */
    @SuppressWarnings("this-escape")
    public WaterdogCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new WaterdogPluginRegistrationHandler<>());
        ((WaterdogPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.senderMapper = senderMapper;

        /* Register WaterdogPE Preprocessor */
        this.registerCommandPreProcessor(new WaterdogCommandPreprocessor<>(this));

        /* Register WaterdogPE Parsers */
        this.parserRegistry()
                .registerParser(PlayerParser.playerParser())
                .registerParser(ServerParser.serverParser());

        /* Register default captions */
        this.captionRegistry()
            .registerProvider(CaptionProvider.<C>constantProvider()
                .putCaption(WaterdogCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, ARGUMENT_PARSE_FAILURE_PLAYER)
                .putCaption(WaterdogCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER, ARGUMENT_PARSE_FAILURE_SERVER)
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
                commandSender.sendMessage(RED_COLOR_CODE + message);
            },
            pair -> this.owningPlugin.getLogger().error(pair.first(), pair.second())
        );
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }
}
