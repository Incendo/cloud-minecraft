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

import java.util.concurrent.Executor;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.suggestion.SuggestionListener;
import org.incendo.cloud.paper.suggestion.SuggestionListenerFactory;
import org.incendo.cloud.state.RegistrationState;

/**
 * {@link CommandManager} implementation for Bukkit-based platforms (i.e. Spigot, Paper),
 * with specific support for Paper features (gated behind {@link CloudBukkitCapabilities} for
 * "backwards-compatibility").
 *
 * <p>This command manager uses legacy Bukkit command APIs. It's recommended to use
 * the {@link PaperCommandManager} instead when supporting Paper 1.20.6+ exclusively.</p>
 *
 * @param <C> command sender type
 * @see LegacyPaperCommandManager#LegacyPaperCommandManager(Plugin, ExecutionCoordinator, SenderMapper)
 * @see #createNative(Plugin, ExecutionCoordinator)
 */
public class LegacyPaperCommandManager<C> extends BukkitCommandManager<C> {

    private @Nullable BrigadierManagerHolder<C, ?> brigadierManagerHolder = null;

    /**
     * Create a new Paper command manager.
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
     *                                    Paper's asynchronous completion API can also
     *                                    be used to avoid this issue: {@link #registerAsynchronousCompletions()}
     * @param senderMapper                Mapper between Bukkit's {@link CommandSender} and the command sender type {@code C}.
     * @see #registerBrigadier()
     * @throws InitializationException if construction of the manager fails
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings("this-escape")
    public LegacyPaperCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) throws InitializationException {
        super(owningPlugin, commandExecutionCoordinator, senderMapper);

        this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(
            this,
            this.senderMapper(),
            Function.identity()
        ));
    }

    /**
     * Create a command manager using Bukkit's {@link CommandSender} as the sender type.
     *
     * @param owningPlugin                plugin owning the command manager
     * @param commandExecutionCoordinator execution coordinator instance
     * @return a new command manager
     * @throws InitializationException if the construction of the manager fails
     * @see #LegacyPaperCommandManager(Plugin, ExecutionCoordinator, SenderMapper) for a more thorough explanation
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull LegacyPaperCommandManager<@NonNull CommandSender> createNative(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<CommandSender> commandExecutionCoordinator
    ) throws InitializationException {
        return new LegacyPaperCommandManager<>(
                owningPlugin,
                commandExecutionCoordinator,
                SenderMapper.identity()
        );
    }

    /**
     * Attempts to enable Brigadier command registration through the Paper API, falling
     * back to {@link BukkitCommandManager#registerBrigadier()} if that fails.
     *
     * <p>Callers should check for {@link CloudBukkitCapabilities#NATIVE_BRIGADIER} first
     * to avoid exceptions.</p>
     *
     * <p>A check for {@link CloudBukkitCapabilities#NATIVE_BRIGADIER} {@code ||} {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER}
     * may also be appropriate for some use cases (because of the fallback behavior), but not most, as Commodore does not offer
     * any functionality on modern
     * versions (see the documentation for {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER}).</p>
     *
     * @see #hasCapability(CloudCapability)
     * @throws BrigadierInitializationException when the prerequisite capabilities are not present or some other issue occurs
     * during registration of Brigadier support
     */
    @Override
    public synchronized void registerBrigadier() throws BrigadierInitializationException {
        this.registerBrigadier(true);
    }

    /**
     * Variant of {@link #registerBrigadier()} that only uses the old Paper-MojangAPI, even
     * when the modern Paper commands API is present. This may be useful for debugging issues
     * with the new Paper command system.
     *
     * @throws BrigadierInitializationException when the prerequisite capabilities are not present or some other issue occurs
     * during registration of Brigadier support
     * @deprecated This method will continue to work while the Paper commands API is still incubating, but will eventually no
     * longer function when the old API is removed.
     */
    @Deprecated
    public synchronized void registerLegacyPaperBrigadier() throws BrigadierInitializationException {
        this.registerBrigadier(false);
    }

    private void registerBrigadier(final boolean allowModern) {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();

        if (this.brigadierManagerHolder != null) {
            throw new IllegalStateException("Brigadier is already registered! Holder: " + this.brigadierManagerHolder);
        }

        if (!this.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            super.registerBrigadier();
        } else if (allowModern && CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.CommandSourceStack")) {
            try {
                final ModernPaperBrigadier<C, CommandSender> brig = new ModernPaperBrigadier<>(
                    CommandSender.class,
                    this,
                    this.senderMapper(),
                    this::lockRegistration
                );
                this.brigadierManagerHolder = brig;
                brig.registerPlugin(this.owningPlugin());
                this.commandRegistrationHandler(brig);
            } catch (final Exception e) {
                throw new BrigadierInitializationException("Failed to register ModernPaperBrigadier", e);
            }
        } else {
            try {
                this.brigadierManagerHolder = new LegacyPaperBrigadier<>(this);
                Bukkit.getPluginManager().registerEvents((Listener) this.brigadierManagerHolder, this.owningPlugin());
                this.brigadierManagerHolder.brigadierManager().settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
            } catch (final Exception e) {
                throw new BrigadierInitializationException("Failed to register LegacyPaperBrigadier", e);
            }
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
        return this.brigadierManagerHolder != null || super.hasBrigadierManager();
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
        if (this.brigadierManagerHolder != null) {
            return this.brigadierManagerHolder.brigadierManager();
        }
        return super.brigadierManager();
    }

    /**
     * Registers asynchronous completions using the Paper API. This means the calling thread for suggestion queries will be a
     * thread other than the {@link Server#isPrimaryThread() main server thread} (or, the sender's thread context on Folia).
     *
     * <p>Requires the {@link CloudBukkitCapabilities#ASYNCHRONOUS_COMPLETION} capability to be present.</p>
     *
     * <p>It's not recommended to use this in combination with {@link #registerBrigadier()}, as Brigadier allows for
     * non-blocking suggestions and the async completion API reduces the fidelity of suggestions compared to using Brigadier
     * directly (see {@link LegacyPaperCommandManager#LegacyPaperCommandManager(Plugin, ExecutionCoordinator, SenderMapper)}).</p>
     *
     * @throws IllegalStateException when the server does not support asynchronous completions
     * @see #hasCapability(CloudCapability)
     */
    public void registerAsynchronousCompletions() throws IllegalStateException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        if (!this.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            throw new IllegalStateException("Failed to register asynchronous command completion listener.");
        }

        final SuggestionListenerFactory<C> suggestionListenerFactory = SuggestionListenerFactory.create(this);
        final SuggestionListener<C> suggestionListener = suggestionListenerFactory.createListener();

        Bukkit.getServer().getPluginManager().registerEvents(
                suggestionListener,
                this.owningPlugin()
        );
    }
}
