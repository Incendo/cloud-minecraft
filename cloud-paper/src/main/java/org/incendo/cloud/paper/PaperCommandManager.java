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

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apiguardian.api.API;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.bukkit.BukkitDefaultCaptionsProvider;
import org.incendo.cloud.bukkit.BukkitParsers;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;

/**
 * A {@link CommandManager} implementation for modern Paper API, using {@link CommandSourceStack} as the base sender type.
 *
 * <p>This manager will only function on servers implementing Paper API 1.20.6 or newer.</p>
 *
 * @param <C> command sender type
 * @see #builder()
 * @see #builder(SenderMapper)
 */
@API(status = API.Status.EXPERIMENTAL)
@SuppressWarnings("UnstableApiUsage")
public class PaperCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSourceStack, C>,
    PluginMetaHolder, PluginHolder, BrigadierManagerHolder<C, CommandSourceStack> {
    private final PluginMeta pluginMeta;
    private final SenderMapper<CommandSourceStack, C> senderMapper;

    /**
     * Creates a new {@link Builder} for a manager with sender type {@link C}.
     *
     * @param senderMapper sender mapper
     * @param <C>          command sender type
     * @return builder
     */
    public static <C> Builder<C> builder(final SenderMapper<CommandSourceStack, C> senderMapper) {
        return new Builder<>(senderMapper);
    }

    /**
     * Creates a new {@link Builder} using the native Paper {@link CommandSourceStack} sender type.
     *
     * @return builder
     */
    public static Builder<CommandSourceStack> builder() {
        return new Builder<>(SenderMapper.identity());
    }

    private PaperCommandManager(
        final @NonNull PluginMeta pluginMeta,
        final @NonNull ExecutionCoordinator<C> executionCoordinator,
        final @NonNull SenderMapper<CommandSourceStack, C> senderMapper
    ) {
        super(executionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.pluginMeta = pluginMeta;
        this.senderMapper = senderMapper;

        this.commandRegistrationHandler(new ModernPaperBrigadier<>(
            CommandSourceStack.class,
            this,
            senderMapper,
            this::lockRegistration
        ));

        CloudBukkitCapabilities.CAPABLE.forEach(this::registerCapability);
        this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);

        BukkitParsers.register(this);

        this.registerDefaultExceptionHandlers();
        this.captionRegistry().registerProvider(new BukkitDefaultCaptionsProvider<>());

        this.registerCommandPreProcessor(ctx -> ctx.commandContext().store(
            BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER,
            this.senderMapper().reverse(ctx.commandContext().sender()).getSender()
        ));
        this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(
            this,
            this.senderMapper(),
            commandSourceStack -> {
                final @Nullable Entity executor = commandSourceStack.getExecutor();
                if (executor != null) {
                    return executor;
                }
                return commandSourceStack.getSender();
            }
        ));
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.senderMapper().reverse(sender).getSender().hasPermission(permission);
    }

    @Override
    public final @NonNull SenderMapper<CommandSourceStack, C> senderMapper() {
        return this.senderMapper;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
            triplet -> this.senderMapper().reverse(triplet.first().sender()).getSender()
                .sendMessage(Component.text(
                    triplet.first().formatCaption(triplet.second(), triplet.third()),
                    NamedTextColor.RED
                )),
            pair -> this.owningPlugin().getLogger().log(Level.SEVERE, pair.first(), pair.second())
        );
    }

    @Override
    public final PluginMeta owningPluginMeta() {
        return this.pluginMeta;
    }

    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final @NonNull CloudBrigadierManager<C, ? extends CommandSourceStack> brigadierManager() {
        return ((BrigadierManagerHolder<C, CommandSourceStack>) this.commandRegistrationHandler())
            .brigadierManager();
    }

    /**
     * Variant of {@link PaperCommandManager} created at
     * {@link io.papermc.paper.plugin.bootstrap.PluginBootstrap bootstrap} time
     * rather than in {@link Plugin#onEnable()}. This allows command registered at bootstrap time to be used by
     * data pack functions.
     *
     * @param <C> command sender type
     */
    public static final class Bootstrapped<C> extends PaperCommandManager<C> {
        private Bootstrapped(
            final @NonNull PluginMeta pluginMeta,
            final @NonNull ExecutionCoordinator<C> executionCoordinator,
            final @NonNull SenderMapper<CommandSourceStack, C> senderMapper
        ) {
            super(pluginMeta, executionCoordinator, senderMapper);
        }

        /**
         * Runs the second phase of initialization for managers created at bootstrap time.
         *
         * <p>This method must be called in {@link Plugin#onEnable()} for some features to work.</p>
         */
        public void onEnable() {
            /*
            ((ModernPaperBrigadier<CommandSourceStack, C>) this.commandRegistrationHandler())
                .registerPlugin(this.owningPlugin());
             */
        }
    }

    /**
     * First stage builder for {@link PaperCommandManager}.
     *
     * @param <C> command sender type
     */
    public static final class Builder<C> {
        private final SenderMapper<CommandSourceStack, C> senderMapper;

        private Builder(final SenderMapper<CommandSourceStack, C> senderMapper) {
            this.senderMapper = senderMapper;
        }

        /**
         * Configures the {@link ExecutionCoordinator} for the manager.
         *
         * @param executionCoordinator execution coordinator
         * @return coordinated builder
         */
        public CoordinatedBuilder<C> executionCoordinator(final ExecutionCoordinator<C> executionCoordinator) {
            return new CoordinatedBuilder<>(this.senderMapper, executionCoordinator);
        }
    }

    /**
     * Second stage builder for {@link PaperCommandManager}.
     *
     * @param <C> command sender type
     */
    public static final class CoordinatedBuilder<C> {
        private final SenderMapper<CommandSourceStack, C> senderMapper;
        private final ExecutionCoordinator<C> executionCoordinator;

        private CoordinatedBuilder(
            final SenderMapper<CommandSourceStack, C> senderMapper,
            final ExecutionCoordinator<C> executionCoordinator
        ) {
            this.senderMapper = senderMapper;
            this.executionCoordinator = executionCoordinator;
        }

        /**
         * Creates a {@link PaperCommandManager} from {@link Plugin#onEnable()}.
         *
         * @param plugin plugin instance
         * @return manager
         * @see Bootstrapped
         * @see #buildBootstrapped(BootstrapContext)
         */
        @SuppressWarnings("unchecked")
        public @NonNull PaperCommandManager<C> buildOnEnable(final @NonNull Plugin plugin) {
            final PaperCommandManager<C> mgr =
                new PaperCommandManager<>(plugin.getPluginMeta(), this.executionCoordinator, this.senderMapper);
            ((ModernPaperBrigadier<CommandSourceStack, C>) mgr.commandRegistrationHandler()).registerPlugin(plugin);
            return mgr;
        }

        /**
         * Creates a {@link PaperCommandManager.Bootstrapped} during bootstrapping.
         *
         * @param context bootstrap context
         * @return manager
         * @see Bootstrapped#onEnable()
         */
        @SuppressWarnings("unchecked")
        public PaperCommandManager.@NonNull Bootstrapped<C> buildBootstrapped(final @NonNull BootstrapContext context) {
            final PaperCommandManager.Bootstrapped<C> mgr =
                new PaperCommandManager.Bootstrapped<>(context.getPluginMeta(), this.executionCoordinator, this.senderMapper);
            ((ModernPaperBrigadier<CommandSourceStack, C>) mgr.commandRegistrationHandler()).registerBootstrap(context);
            return mgr;
        }
    }
}
