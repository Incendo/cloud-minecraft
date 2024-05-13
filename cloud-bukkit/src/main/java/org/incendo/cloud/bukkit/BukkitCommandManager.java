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
package org.incendo.cloud.bukkit;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.apiguardian.api.API;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.state.RegistrationState;

/**
 * Base {@link CommandManager} implementation for Bukkit-based platforms.
 *
 * @param <C> command sender type
 */
public abstract class BukkitCommandManager<C> extends CommandManager<C> implements BrigadierManagerHolder<C, Object>,
    SenderMapperHolder<CommandSender, C>, PluginHolder {

    private final Plugin owningPlugin;
    private final SenderMapper<CommandSender, C> senderMapper;

    private boolean splitAliases = false;

    /**
     * Create a new Bukkit command manager. {@link BukkitCommandManager} is not intended to be created and used directly.
     * Instead, use {@code PaperCommandManager} from {@code cloud-paper} which extends {@link BukkitCommandManager} with specific
     * support for Paper-based platforms, but does not lose support for non Paper-based Bukkit platforms.
     *
     * @param owningPlugin                Plugin constructing the manager. Used when registering commands to the command map,
     *                                    registering event listeners, etc.
     * @param commandExecutionCoordinator Execution coordinator instance. Due to Bukkit blocking the main thread for
     *                                    suggestion requests, it's potentially unsafe to use anything other than
     *                                    {@link ExecutionCoordinator#nonSchedulingExecutor()} for
     *                                    {@link ExecutionCoordinator.Builder#suggestionsExecutor(Executor)}. Once the
     *                                    coordinator, a suggestion provider, parser, or similar routes suggestion logic
     *                                    off of the calling (main) thread, it won't be possible to schedule further logic
     *                                    back to the main thread without a deadlock. When Brigadier support is active, this issue
     *                                    is avoided, as it allows for non-blocking suggestions.
     * @param senderMapper                Mapper between Bukkit's {@link CommandSender} and the command sender type {@code C}.
     * @see #registerBrigadier()
     * @throws InitializationException if construction of the manager fails
     */
    @API(status = API.Status.INTERNAL, since = "2.0.0")
    @SuppressWarnings("this-escape")
    protected BukkitCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) throws InitializationException {
        super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());
        try {
            ((BukkitPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        } catch (final ReflectiveOperationException exception) {
            throw new InitializationException("Failed to initialize command registration handler", exception);
        }
        this.owningPlugin = owningPlugin;
        this.senderMapper = senderMapper;

        /* Register capabilities */
        CloudBukkitCapabilities.CAPABLE.forEach(this::registerCapability);
        this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);

        /* Register Bukkit Preprocessor */
        this.registerCommandPreProcessor(new BukkitCommandPreprocessor<>(this));

        BukkitParsers.register(this);

        /* Register suggestion and state listener */
        this.owningPlugin.getServer().getPluginManager().registerEvents(
                new CloudBukkitListener<>(this),
                this.owningPlugin
        );

        this.registerDefaultExceptionHandlers();
        this.captionRegistry().registerProvider(new BukkitDefaultCaptionsProvider<>());
    }

    @Override
    public final @NonNull Plugin owningPlugin() {
        return this.owningPlugin;
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    protected final boolean splitAliases() {
        return this.splitAliases;
    }

    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    protected final void splitAliases(final boolean value) {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.splitAliases = value;
    }

    /**
     * Check whether Brigadier can be used on the server instance
     *
     * @throws BrigadierInitializationException An exception is thrown if Brigadier isn't available. The exception
     *                                   will contain the reason for this.
     */
    protected final void checkBrigadierCompatibility() throws BrigadierInitializationException {
        if (!this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            throw new BrigadierInitializationException(
                    "Missing capability " + CloudBukkitCapabilities.class.getSimpleName() + "."
                            + CloudBukkitCapabilities.BRIGADIER + " (Minecraft version too old? Brigadier was added in 1.13). "
                            + "See the Javadocs for more details"
            );
        }
    }

    /**
     * Attempts to enable Brigadier command registration through Commodore.
     *
     * <p>Callers should check for {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER} first
     * to avoid exceptions.</p>
     *
     * @see #hasCapability(CloudCapability)
     * @throws BrigadierInitializationException when the prerequisite capabilities are not present or some other issue occurs
     * during registration of Brigadier support
     */
    public synchronized void registerBrigadier() throws BrigadierInitializationException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        if (!this.hasCapability(CloudBukkitCapabilities.COMMODORE_BRIGADIER)) {
            throw new BrigadierInitializationException(
                    "Missing capability " + CloudBukkitCapabilities.class.getSimpleName() + "."
                            + CloudBukkitCapabilities.COMMODORE_BRIGADIER + " (Minecraft version too new). "
                            + "See the Javadocs for more details"
            );
        }
        final CommandRegistrationHandler<C> handler = this.commandRegistrationHandler();
        if (handler instanceof CloudCommodoreManager) {
            throw new IllegalStateException("Brigadier is already registered! Holder: " + handler);
        }
        try {
            final CloudCommodoreManager<C> cloudCommodoreManager = new CloudCommodoreManager<>(this);
            cloudCommodoreManager.initialize(this);
            this.commandRegistrationHandler(cloudCommodoreManager);
            this.splitAliases(true);
        } catch (final Exception e) {
            throw new BrigadierInitializationException(
                    "Unexpected exception initializing " + CloudCommodoreManager.class.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public boolean hasBrigadierManager() {
        return this.commandRegistrationHandler() instanceof CloudCommodoreManager;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws BrigadierManagerNotPresent when {@link #hasBrigadierManager()} is false
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public @NonNull CloudBrigadierManager<C, ?> brigadierManager() {
        if (this.commandRegistrationHandler() instanceof CloudCommodoreManager) {
            return ((CloudCommodoreManager<C>) this.commandRegistrationHandler()).brigadierManager();
        }
        throw new BrigadierManagerHolder.BrigadierManagerNotPresent("The CloudBrigadierManager is either not supported in the "
                + "current environment, or it is not enabled.");
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
            triplet -> this.senderMapper().reverse(triplet.first().sender())
                .sendMessage(ChatColor.RED + triplet.first().formatCaption(triplet.second(), triplet.third())),
            pair -> this.owningPlugin().getLogger().log(Level.SEVERE, pair.first(), pair.second())
        );
    }

    final void lockIfBrigadierCapable() {
        if (this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.lockRegistration();
        }
    }


    /**
     * Exception thrown when the command manager could not be initialized.
     *
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static final class InitializationException extends IllegalStateException {

        /**
         * Create a new {@link InitializationException}.
         *
         * @param message message
         * @param cause   cause
         */
        @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
        public InitializationException(final String message, final @Nullable Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception thrown when Brigadier mappings fail to initialize.
     *
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static final class BrigadierInitializationException extends IllegalStateException {

        /**
         * Creates a new Brigadier failure exception.
         *
         * @param reason Reason
         */
        @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
        public BrigadierInitializationException(final @NonNull String reason) {
            super(reason);
        }

        /**
         * Creates a new Brigadier failure exception.
         *
         * @param reason Reason
         * @param cause  Cause
         */
        @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
        public BrigadierInitializationException(final @NonNull String reason, final @Nullable Throwable cause) {
            super(reason, cause);
        }
    }
}
