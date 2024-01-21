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
package cloud.commandframework.sponge.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.sponge.NodeSource;
import cloud.commandframework.sponge.SpongeCaptionKeys;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProviders;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;

/**
 * An argument for retrieving values from any of Sponge's {@link Registry Registries}.
 *
 * @param <C> command sender type
 * @param <V> value type
 */
public final class RegistryEntryParser<C, V> implements NodeSource,
    ArgumentParser.FutureArgumentParser<C, V>, BlockingSuggestionProvider.Strings<C> {

    // Start DefaultedRegistryType methods

    /**
     * Create a new {@link RegistryEntryParser} for a {@link DefaultedRegistryType}.
     *
     * @param <C>          command sender type
     * @param <V>          value type
     * @param valueType    value type
     * @param registryType registry type
     * @return a new {@link RegistryEntryParser}
     */
    public static <C, V> @NonNull ParserDescriptor<C, V> registryEntryParser(
        final @NonNull TypeToken<V> valueType,
        final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return ParserDescriptor.of(new RegistryEntryParser<>(registryType), valueType);
    }

    /**
     * Create a new {@link RegistryEntryParser} for a {@link DefaultedRegistryType}.
     *
     * @param <C>          command sender type
     * @param <V>          value type
     * @param valueType    value type
     * @param registryType registry type
     * @return a new {@link RegistryEntryParser}
     */
    public static <C, V> @NonNull ParserDescriptor<C, V> registryEntryParser(
        final @NonNull Class<V> valueType,
        final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return ParserDescriptor.of(new RegistryEntryParser<>(registryType), valueType);
    }

    // End DefaultedRegistryType methods

    // Start RegistryType methods

    /**
     * Create a new {@link RegistryEntryParser} for a {@link RegistryType}
     * using the specified {@link RegistryHolder} function.
     *
     * <p>For {@link RegistryType RegistryTypes} which are {@link DefaultedRegistryType DefaultedRegistryTypes},
     * it is suggested to instead use {@link #registryEntryParser(TypeToken, DefaultedRegistryType)}.</p>
     *
     * @param <C>            command sender type
     * @param <V>            value type
     * @param valueType      value type
     * @param registryType   registry type
     * @param holderSupplier registry holder function
     * @return a new {@link RegistryEntryParser}
     */
    public static <C, V> @NonNull ParserDescriptor<C, V> registryEntryParser(
        final @NonNull Class<V> valueType,
        final @NonNull RegistryType<V> registryType,
        final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier
    ) {
        return ParserDescriptor.of(new RegistryEntryParser<>(holderSupplier, registryType), valueType);
    }

    /**
     * Create a new {@link RegistryEntryParser} for a {@link RegistryType}
     * using the specified {@link RegistryHolder} function.
     *
     * <p>For {@link RegistryType RegistryTypes} which are {@link DefaultedRegistryType DefaultedRegistryTypes},
     * it is suggested to instead use {@link #registryEntryParser(TypeToken, DefaultedRegistryType)}.</p>
     *
     * @param <C>            command sender type
     * @param <V>            value type
     * @param valueType      value type
     * @param registryType   registry type
     * @param holderSupplier registry holder function
     * @return a new {@link RegistryEntryParser}
     */
    public static <C, V> @NonNull ParserDescriptor<C, V> registryEntryParser(
        final @NonNull TypeToken<V> valueType,
        final @NonNull RegistryType<V> registryType,
        final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier
    ) {
        return ParserDescriptor.of(new RegistryEntryParser<>(holderSupplier, registryType), valueType);
    }

    // End RegistryType methods

    private static final ArgumentParser<?, ResourceKey> RESOURCE_KEY_PARSER = new ResourceKeyParser<>();

    private final Function<CommandContext<C>, RegistryHolder> holderSupplier;
    private final RegistryType<V> registryType;

    /**
     * Create a new {@link RegistryEntryParser} using the specified {@link RegistryHolder} function.
     *
     * <p>For {@link RegistryType RegistryTypes} which are {@link DefaultedRegistryType DefaultedRegistryTypes},
     * it is suggested to instead use {@link #RegistryEntryParser(DefaultedRegistryType)}.</p>
     *
     * @param holderSupplier registry holder function
     * @param registryType   registry type
     */
    public RegistryEntryParser(
        final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
        final @NonNull RegistryType<V> registryType
    ) {
        this.holderSupplier = holderSupplier;
        this.registryType = registryType;
    }

    /**
     * Create a new {@link RegistryEntryParser}.
     *
     * @param registryType defaulted registry type
     */
    public RegistryEntryParser(final @NonNull DefaultedRegistryType<V> registryType) {
        this(ctx -> registryType.defaultHolder().get(), registryType);
    }

    private Registry<V> registry(final @NonNull CommandContext<C> commandContext) {
        return this.holderSupplier.apply(commandContext).registry(this.registryType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<@NonNull V>> parseFuture(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
    ) {
        return ((ArgumentParser<C, ResourceKey>) RESOURCE_KEY_PARSER).parseFuture(commandContext, inputQueue).thenApply(keyResult -> {
            if (keyResult.failure().isPresent()) {
                return ArgumentParseResult.failure(keyResult.failure().get());
            }
            final Optional<RegistryEntry<V>> entry = this.registry(commandContext).findEntry(keyResult.parsedValue().get());
            if (entry.isPresent()) {
                return ArgumentParseResult.success(entry.get().value());
            }
            return ArgumentParseResult.failure(new NoSuchEntryException(commandContext, keyResult.parsedValue().get(), this.registryType));
        });
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return this.registry(commandContext).streamEntries().flatMap(entry -> {
            if (!input.isEmpty() && entry.key().namespace().equals(ResourceKey.MINECRAFT_NAMESPACE)) {
                return Stream.of(entry.key().value(), entry.key().asString());
            }
            return Stream.of(entry.key().asString());
        }).collect(Collectors.toList());
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        if (this.registryType.equals(RegistryTypes.SOUND_TYPE)) {
            return CommandTreeNodeTypes.RESOURCE_LOCATION.get().createNode()
                .completions(CommandCompletionProviders.AVAILABLE_SOUNDS);
        } else if (this.registryType.equals(RegistryTypes.BIOME)) {
            return CommandTreeNodeTypes.RESOURCE_LOCATION.get().createNode()
                .completions(CommandCompletionProviders.AVAILABLE_BIOMES);
        } else if (this.registryType.equals(RegistryTypes.ENTITY_TYPE)) {
            return CommandTreeNodeTypes.ENTITY_SUMMON.get().createNode()
                .completions(CommandCompletionProviders.SUMMONABLE_ENTITIES);
        } else if (this.registryType.equals(RegistryTypes.ENCHANTMENT_TYPE)) {
            return CommandTreeNodeTypes.ITEM_ENCHANTMENT.get().createNode();
        } else if (this.registryType.equals(RegistryTypes.POTION_EFFECT_TYPE)) {
            return CommandTreeNodeTypes.MOB_EFFECT.get().createNode();
        } else if (this.registryType.equals(RegistryTypes.WORLD_TYPE)) {
            return CommandTreeNodeTypes.DIMENSION.get().createNode()
                .customCompletions(); // Sponge adds custom types (?)
        }
        return CommandTreeNodeTypes.RESOURCE_LOCATION.get().createNode().customCompletions();
    }

    /**
     * An exception thrown when there is no entry for the provided {@link ResourceKey} in the resolved registry.
     */
    private static final class NoSuchEntryException extends ParserException {

        private static final long serialVersionUID = 4472876671109079272L;

        NoSuchEntryException(
            final CommandContext<?> context,
            final ResourceKey key,
            final RegistryType<?> registryType
        ) {
            super(
                RegistryEntryParser.class,
                context,
                SpongeCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY,
                CaptionVariable.of("id", key.asString()),
                CaptionVariable.of("registry", registryType.location().asString())
            );
        }

    }

}
