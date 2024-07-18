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
package org.incendo.cloud.paper.parser;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.internal.ImmutableImpl;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.MappedArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = API.Status.EXPERIMENTAL)
public final class RegistryEntryParser<C, E extends Keyed>
    implements ArgumentParser<C, RegistryEntryParser.RegistryEntry<E>>, SuggestionProvider<C>,
    MappedArgumentParser<C, NamespacedKey, RegistryEntryParser.RegistryEntry<E>> {

    /**
     * Creates a {@link RegistryEntryParser}.
     *
     * @param registryKey registry key
     * @param elementType registry element type
     * @param <C>         command sender type
     * @param <E>         registry element type
     * @return the created parser
     * @since 2.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, E extends Keyed> @NonNull ParserDescriptor<C, RegistryEntry<E>> registryEntryParser(
        final RegistryKey<E> registryKey,
        final TypeToken<E> elementType
    ) {
        return ParserDescriptor.of(
            new RegistryEntryParser<>(registryKey),
            (TypeToken) TypeToken.get(TypeFactory.parameterizedClass(RegistryEntry.class, elementType.getType()))
        );
    }

    private final ParserDescriptor<C, NamespacedKey> keyParser;
    private final RegistryKey<E> registryKey;

    /**
     * Create a new {@link RegistryEntryParser}.
     *
     * @param registryKey registry key
     */
    public RegistryEntryParser(final RegistryKey<E> registryKey) {
        this.keyParser = NamespacedKeyParser.namespacedKeyParser();
        this.registryKey = registryKey;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public @NonNull ArgumentParseResult<RegistryEntry<@NonNull E>> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        return this.keyParser.parser().parse(commandContext, commandInput).flatMapSuccess(key -> {
            final Registry<E> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey);

            final E value = registry.get(key);
            if (value == null) {
                return ArgumentParseResult.failure(new ParseException(key.asString(), (RegistryKey) this.registryKey, commandContext));
            }

            return ArgumentParseResult.success(RegistryEntryImpl.of(value, key));
        });
    }

    @Override
    public @NonNull ArgumentParser<C, NamespacedKey> baseParser() {
        return this.keyParser.parser();
    }

    /**
     * Exception when there is no registry entry for the provided key.
     */
    public static final class ParseException extends ParserException {
        private final String input;
        private final RegistryKey<Object> registryKey;

        /**
         * Creates a new {@link ParseException}.
         *
         * @param input   input string
         * @param registryKey registry key
         * @param context command context
         */
        public ParseException(
            final @NonNull String input,
            final @NonNull RegistryKey<Object> registryKey,
            final @NonNull CommandContext<?> context
        ) {
            super(
                RegistryEntryParser.class,
                context,
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_MISSING,
                CaptionVariable.of("input", input),
                CaptionVariable.of("registry", registryKey.key().asString())
            );
            this.input = input;
            this.registryKey = registryKey;
        }

        /**
         * Get the input provided by the sender
         *
         * @return Input
         */
        public @NonNull String input() {
            return this.input;
        }

        /**
         * Returns the registry key.
         *
         * @return registry key
         */
        public @NonNull RegistryKey<Object> registryKey() {
            return this.registryKey;
        }
    }

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        final List<Suggestion> completions = new ArrayList<>();
        final Registry<E> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey);
        registry.stream()
            .map(registry::getKeyOrThrow)
            .forEach(key -> {
                if (input.hasRemainingInput() && key.getNamespace().equals(NamespacedKey.MINECRAFT_NAMESPACE)) {
                    completions.add(Suggestion.suggestion(key.getKey()));
                }
                completions.add(Suggestion.suggestion(key.getNamespace() + ':' + key.getKey()));
            });
        return CompletableFuture.completedFuture(completions);
    }

    /**
     * Holds a registry value and it's key.
     *
     * @param <E> value type
     */
    @ImmutableImpl
    @Value.Immutable
    public interface RegistryEntry<E> {
        /**
         * Returns the value.
         *
         * @return the value
         */
        E value();

        /**
         * Returns the key.
         *
         * @return the key
         */
        NamespacedKey key();
    }
}
