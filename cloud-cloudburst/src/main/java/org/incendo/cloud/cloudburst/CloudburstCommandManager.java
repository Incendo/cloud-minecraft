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
package org.incendo.cloud.cloudburst;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.event.EventPriority;
import org.cloudburstmc.server.event.Listener;
import org.cloudburstmc.server.event.server.RegistriesClosedEvent;
import org.cloudburstmc.server.plugin.Plugin;
import org.cloudburstmc.server.utils.TextFormat;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.signed.SignedArguments;
import org.incendo.cloud.state.RegistrationState;

/**
 * Command manager for the Cloudburst platform
 *
 * @param <C> Command sender type
 */
public class CloudburstCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSender, C> {

    private final SenderMapper<CommandSender, C> senderMapper;

    private final Plugin owningPlugin;

    /**
     * Construct a new Cloudburst command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param senderMapper                 Function that maps {@link CommandSender} to the command sender type
     */
    @SuppressWarnings("unchecked")
    public CloudburstCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new CloudburstPluginRegistrationHandler<>());
        ((CloudburstPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.senderMapper = senderMapper;
        this.owningPlugin = owningPlugin;
        this.parameterInjectorRegistry().registerInjector(
                CommandSender.class,
                (context, annotations) -> this.senderMapper.reverse(context.sender())
        );
        SignedArguments.registerParser(this);

        // Prevent commands from being registered when the server would reject them anyways
        this.owningPlugin.getServer().getPluginManager().registerEvent(
                RegistriesClosedEvent.class,
                CloudListener.INSTANCE,
                EventPriority.NORMAL,
                (listener, event) -> this.lockRegistration(),
                this.owningPlugin
        );

        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender,
            final @NonNull String permission
    ) {
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    @Override
    public final boolean isCommandRegistrationAllowed() {
        return this.state() != RegistrationState.AFTER_REGISTRATION;
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    public final @NonNull Plugin owningPlugin() {
        return this.owningPlugin;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
            triplet -> {
                final CommandSender commandSender = triplet.first().inject(CommandSender.class)
                    .orElseThrow(NullPointerException::new);
                final String message = triplet.first().formatCaption(triplet.second(), triplet.third());
                commandSender.sendMessage(TextFormat.RED + message);
            },
            pair -> this.owningPlugin().getLogger().error(pair.first(), pair.second())
        );
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }


    static final class CloudListener implements Listener {

        static final CloudListener INSTANCE = new CloudListener();

        private CloudListener() {
        }
    }
}
