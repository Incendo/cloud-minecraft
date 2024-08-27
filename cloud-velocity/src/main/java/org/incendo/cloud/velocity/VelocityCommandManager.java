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
package org.incendo.cloud.velocity;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.suggestion.SuggestionFactory;
import org.incendo.cloud.velocity.parser.PlayerParser;
import org.incendo.cloud.velocity.parser.ServerParser;

/**
 * {@link CommandManager} implementation for Velocity.
 * <p>
 * This can be injected if {@link CloudInjectionModule} is registered in the
 * injector. This can be achieved by using {@link com.google.inject.Injector#createChildInjector(Module...)}
 * <p>
 * {@link #suggestionFactory()} has been overridden to map suggestions to {@link TooltipSuggestion}.
 * You may use {@link TooltipSuggestion} to display tooltips for your suggestions.
 * {@link com.velocitypowered.api.command.VelocityBrigadierMessage} can be used to make use of Adventure
 * {@link net.kyori.adventure.text.Component components} in the tooltips.
 *
 * @param <C> Command sender type
 */
@Singleton
public class VelocityCommandManager<C> extends CommandManager<C>
        implements BrigadierManagerHolder<C, CommandSource>, SenderMapperHolder<CommandSource, C> {

    /**
     * Default caption for {@link VelocityCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'<input>' is not a valid player";

    /**
     * Default caption for {@link VelocityCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'<input>' is not a valid server";

    private final ProxyServer proxyServer;
    private final SenderMapper<CommandSource, C> senderMapper;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

    /**
     * Create a new command manager instance
     *
     * @param plugin                       Container for the owning plugin
     * @param proxyServer                  ProxyServer instance
     * @param commandExecutionCoordinator  Coordinator provider
     * @param senderMapper                 Function that maps {@link CommandSource} to the command sender type
     */
    @Inject
    @SuppressWarnings({"unchecked", "this-escape"})
    public VelocityCommandManager(
            final @NonNull PluginContainer plugin,
            final @NonNull ProxyServer proxyServer,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSource, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new VelocityPluginRegistrationHandler<>());
        this.proxyServer = proxyServer;
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);

        ((VelocityPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);

        /* Register Velocity Preprocessor */
        this.registerCommandPreProcessor(new VelocityCommandPreprocessor<>(this));

        /* Register Velocity Parsers */
        this.parserRegistry()
                .registerParser(PlayerParser.playerParser())
                .registerParser(ServerParser.serverParser());

        /* Register default captions */
        this.captionRegistry()
            .registerProvider(CaptionProvider.<C>constantProvider()
                .putCaption(VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, ARGUMENT_PARSE_FAILURE_PLAYER)
                .putCaption(VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER, ARGUMENT_PARSE_FAILURE_SERVER)
                .build());

        this.proxyServer.getEventManager().register(plugin, ServerPreConnectEvent.class, ev -> {
            this.lockRegistration();
        });
        this.parameterInjectorRegistry().registerInjector(
                CommandSource.class,
                (context, annotations) -> this.senderMapper.reverse(context.sender())
        );

        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This will always return true for {@link VelocityCommandManager}s.</p>
     *
     * @return {@code true}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link VelocityCommandManager}s always use Brigadier for registration, so the aforementioned check is not needed.</p>
     *
     * @return {@inheritDoc}
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final @NonNull CloudBrigadierManager<C, CommandSource> brigadierManager() {
        return ((VelocityPluginRegistrationHandler<C>) this.commandRegistrationHandler()).brigadierManager();
    }

    @Override
    public final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    final @NonNull ProxyServer proxyServer() {
        return this.proxyServer;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
            triplet -> this.senderMapper().reverse(triplet.first().sender())
                .sendMessage(Component.text(triplet.first().formatCaption(triplet.second(), triplet.third()), NamedTextColor.RED)),
            pair -> pair.second().printStackTrace()
        );
    }

    @Override
    public final @NonNull SenderMapper<CommandSource, C> senderMapper() {
        return this.senderMapper;
    }
}
