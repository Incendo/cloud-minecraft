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

import cloud.commandframework.CommandManager;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.SenderMapperHolder;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.sponge.annotation.specifier.Center;
import cloud.commandframework.sponge.parser.RegistryEntryParser;
import cloud.commandframework.sponge.parser.Vector2dParser;
import cloud.commandframework.sponge.parser.Vector3dParser;
import cloud.commandframework.state.RegistrationState;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;

import static cloud.commandframework.sponge.parser.BlockInputParser.blockInputParser;
import static cloud.commandframework.sponge.parser.BlockPredicateParser.blockPredicateParser;
import static cloud.commandframework.sponge.parser.ComponentParser.componentParser;
import static cloud.commandframework.sponge.parser.DataContainerParser.dataContainerParser;
import static cloud.commandframework.sponge.parser.GameProfileCollectionParser.gameProfileCollectionParser;
import static cloud.commandframework.sponge.parser.GameProfileParser.gameProfileParser;
import static cloud.commandframework.sponge.parser.ItemStackPredicateParser.itemStackPredicateParser;
import static cloud.commandframework.sponge.parser.MultipleEntitySelectorParser.multipleEntitySelectorParser;
import static cloud.commandframework.sponge.parser.MultiplePlayerSelectorParser.multiplePlayerSelectorParser;
import static cloud.commandframework.sponge.parser.NamedTextColorParser.namedTextColorParser;
import static cloud.commandframework.sponge.parser.OperatorParser.operatorParser;
import static cloud.commandframework.sponge.parser.ProtoItemStackParser.protoItemStackParser;
import static cloud.commandframework.sponge.parser.ResourceKeyParser.resourceKeyParser;
import static cloud.commandframework.sponge.parser.SingleEntitySelectorParser.singleEntitySelectorParser;
import static cloud.commandframework.sponge.parser.SinglePlayerSelectorParser.singlePlayerSelectorParser;
import static cloud.commandframework.sponge.parser.UserParser.userParser;
import static cloud.commandframework.sponge.parser.Vector2iParser.vector2iParser;
import static cloud.commandframework.sponge.parser.Vector3iParser.vector3iParser;
import static cloud.commandframework.sponge.parser.WorldParser.worldParser;

/**
 * Command manager for Sponge API v8.
 * <p>
 * The manager supports Guice injection
 * as long as the {@link CloudInjectionModule} is present in the injector.
 * This can be achieved by using {@link com.google.inject.Injector#createChildInjector(Module...)}
 *
 * @param <C> Command sender type
 */
public final class SpongeCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandCause, C> {

    private final PluginContainer pluginContainer;
    private final SenderMapper<CommandCause, C> senderMapper;
    private final SpongeParserMapper<C> parserMapper;

    /**
     * Create a new command manager instance
     *
     * @param pluginContainer      Owning plugin
     * @param executionCoordinator Execution coordinator instance
     * @param senderMapper         Function mapping the custom command sender type to a Sponge CommandCause
     */
    @SuppressWarnings("unchecked")
    @Inject
    public SpongeCommandManager(
        final @NonNull PluginContainer pluginContainer,
        final @NonNull ExecutionCoordinator<C> executionCoordinator,
        final @NonNull SenderMapper<CommandCause, C> senderMapper
    ) {
        super(executionCoordinator, new SpongeRegistrationHandler<C>());
        this.checkLateCreation();
        this.pluginContainer = pluginContainer;
        ((SpongeRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.senderMapper = senderMapper;
        this.parserMapper = new SpongeParserMapper<>();
        this.registerCommandPreProcessor(new SpongeCommandPreprocessor<>(this));
        this.registerParsers();
        this.captionRegistry(new SpongeCaptionRegistry<>());

        CloudSpongeCommand.registerExceptionHandlers(this);
    }

    private void checkLateCreation() {
        // Not the most accurate check, but will at least catch creation attempted after the server has started
        if (!Sponge.isServerAvailable()) {
            return;
        }
        throw new IllegalStateException(
            "SpongeCommandManager must be created before the first firing of RegisterCommandEvent. (created too late)"
        );
    }

    private void registerParsers() {
        this.parserRegistry()
            .registerParser(componentParser())
            .registerParser(namedTextColorParser())
            .registerParser(operatorParser())
            .registerParser(worldParser())
            .registerParser(protoItemStackParser())
            .registerParser(itemStackPredicateParser())
            .registerParser(resourceKeyParser())
            .registerParser(gameProfileParser())
            .registerParser(gameProfileCollectionParser())
            .registerParser(blockInputParser())
            .registerParser(blockPredicateParser())
            .registerParser(userParser())
            .registerParser(dataContainerParser())
            .registerAnnotationMapper(
                Center.class,
                (annotation, type) -> ParserParameters.single(SpongeParserParameters.CENTER_INTEGERS, true)
            )
            .registerParserSupplier(
                TypeToken.get(Vector2d.class),
                params -> new Vector2dParser<>(params.get(SpongeParserParameters.CENTER_INTEGERS, false))
            )
            .registerParserSupplier(
                TypeToken.get(Vector3d.class),
                params -> new Vector3dParser<>(params.get(SpongeParserParameters.CENTER_INTEGERS, false))
            )
            .registerParser(vector2iParser())
            .registerParser(vector3iParser())
            .registerParser(singlePlayerSelectorParser())
            .registerParser(multiplePlayerSelectorParser())
            .registerParser(singleEntitySelectorParser())
            .registerParser(multipleEntitySelectorParser());

        this.registerRegistryParsers();
    }

    private void registerRegistryParsers() {
        final Set<RegistryType<?>> ignoredRegistryTypes = ImmutableSet.of(
            RegistryTypes.OPERATOR // We have a different Operator parser that doesn't use a ResourceKey as input
        );
        for (final Field field : RegistryTypes.class.getDeclaredFields()) {
            final Type generic = field.getGenericType(); /* RegistryType<?> */
            if (!(generic instanceof ParameterizedType)) {
                continue;
            }

            final RegistryType<?> registryType;
            try {
                registryType = (RegistryType<?>) field.get(null);
            } catch (final IllegalAccessException ex) {
                throw new RuntimeException("Failed to access RegistryTypes." + field.getName(), ex);
            }
            if (ignoredRegistryTypes.contains(registryType) || !(registryType instanceof DefaultedRegistryType)) {
                continue;
            }
            final DefaultedRegistryType<?> defaultedRegistryType = (DefaultedRegistryType<?>) registryType;
            final Type valueType = ((ParameterizedType) generic).getActualTypeArguments()[0];

            this.parserRegistry().registerParserSupplier(
                TypeToken.get(valueType),
                params -> new RegistryEntryParser<>(defaultedRegistryType)
            );
        }
    }

    @Override
    public boolean hasPermission(
        @NonNull final C sender,
        @NonNull final String permission
    ) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    @Override
    public @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Get the {@link PluginContainer} of the plugin that owns this command manager.
     *
     * @return plugin container
     */
    public @NonNull PluginContainer owningPluginContainer() {
        return this.pluginContainer;
    }

    /**
     * Get the {@link SpongeParserMapper}, responsible for mapping Cloud
     * {@link cloud.commandframework.arguments.parser.ArgumentParser ArgumentParser} to Sponge
     * {@link org.spongepowered.api.command.registrar.tree.CommandTreeNode.Argument CommandTreeNode.Arguments}.
     *
     * @return the parser mapper
     */
    public @NonNull SpongeParserMapper<C> parserMapper() {
        return this.parserMapper;
    }

    @Override
    public @NonNull SenderMapper<CommandCause, C> senderMapper() {
        return this.senderMapper;
    }

    void registrationCalled() {
        if (!this.registrationCallbackListeners.isEmpty()) {
            this.registrationCallbackListeners.forEach(listener -> listener.accept(this));
            this.registrationCallbackListeners.clear();
        }
        if (this.state() != RegistrationState.AFTER_REGISTRATION) {
            this.lockRegistration();
        }
    }

    private final Set<Consumer<SpongeCommandManager<C>>> registrationCallbackListeners = new HashSet<>();

    /**
     * Add a listener to the command registration callback.
     *
     * <p>These listeners will be called just before command registration is finalized
     * (during the first invocation of Cloud's internal {@link RegisterCommandEvent} listener).</p>
     *
     * <p>This allows for registering commands at the latest possible point in the plugin
     * lifecycle, which may be necessary for certain {@link Registry Registries} to have
     * initialized.</p>
     *
     * @param listener listener
     */
    public void addRegistrationCallbackListener(final @NonNull Consumer<@NonNull SpongeCommandManager<C>> listener) {
        if (this.state() == RegistrationState.AFTER_REGISTRATION) {
            throw new IllegalStateException("The SpongeCommandManager is in the AFTER_REGISTRATION state!");
        }
        this.registrationCallbackListeners.add(listener);
    }

}
