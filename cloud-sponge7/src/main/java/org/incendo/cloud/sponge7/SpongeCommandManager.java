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
package org.incendo.cloud.sponge7;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.key.CloudKey;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static java.util.Objects.requireNonNull;

/**
 * A command manager for SpongeAPI 7.
 *
 * @param <C> the command source type
 * @since 1.4.0
 */
@Singleton
public class SpongeCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSource, C> {

    public static final CloudKey<CommandSource> SPONGE_COMMAND_SOURCE_KEY = CloudKey.of(
            "__internal_commandsource__",
            CommandSource.class
    );

    private final PluginContainer owningPlugin;
    private final SenderMapper<CommandSource, C> senderMapper;

    /**
     * Create a new command manager instance.
     *
     * @param container                   The plugin that owns this command manager
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link ExecutionCoordinator#asyncCoordinator()}
     * @param senderMapper                A function converting from a native {@link CommandSource} to this manager's sender type
     */
    @Inject
    @SuppressWarnings("unchecked")
    public SpongeCommandManager(
            final @NonNull PluginContainer container,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSource, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new SpongePluginRegistrationHandler<>());
        this.owningPlugin = requireNonNull(container, "container");
        this.senderMapper = requireNonNull(senderMapper, "senderMapper");
        ((SpongePluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    final PluginContainer owningPlugin() {
        return this.owningPlugin;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
            triplet -> {
                final CommandSource source = triplet.first().get(SPONGE_COMMAND_SOURCE_KEY);
                final String message = triplet.first().formatCaption(triplet.second(), triplet.third());
                source.sendMessage(Text.of(message, TextColors.RED));
            },
            pair -> this.owningPlugin().getLogger().error(pair.first(), pair.second())
        );
    }

    @Override
    public final @NonNull SenderMapper<CommandSource, C> senderMapper() {
        return this.senderMapper;
    }
}
