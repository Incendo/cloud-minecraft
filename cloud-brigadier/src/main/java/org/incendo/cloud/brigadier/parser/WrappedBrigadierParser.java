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
package org.incendo.cloud.brigadier.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import static java.util.Objects.requireNonNull;

/**
 * A wrapped {@link ArgumentParser argument parser} adapting Brigadier {@link ArgumentType argument types}.
 *
 * @param <C> command sender type
 * @param <T> value type
 * @since 1.5.0
 */
public class WrappedBrigadierParser<C, T> implements ArgumentParser<C, T>, SuggestionProvider<C> {

    public static final String COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER = "_cloud_brigadier_native_sender";

    private final Supplier<ArgumentType<T>> nativeType;
    private final @Nullable ParseFunction<T> parse;

    /**
     * Create an {@link ArgumentParser argument parser} from a Brigadier {@link ArgumentType}.
     *
     * @param argumentType Brigadier argument type
     * @since 1.5.0
     */
    public WrappedBrigadierParser(final ArgumentType<T> argumentType) {
        this(() -> argumentType);
    }

    /**
     * Create a {@link WrappedBrigadierParser} for the {@link ArgumentType argument type}.
     *
     * @param argumentTypeSupplier Brigadier argument type supplier
     * @since 1.7.0
     */
    public WrappedBrigadierParser(final Supplier<ArgumentType<T>> argumentTypeSupplier) {
        this(argumentTypeSupplier, null);
    }

    /**
     * Create an {@link ArgumentParser argument parser} from a Brigadier {@link ArgumentType}.
     *
     * @param argumentTypeSupplier  Brigadier argument type supplier
     * @param parse                 special function to replace {@link ArgumentType#parse(StringReader)} (for CraftBukkit weirdness)
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public WrappedBrigadierParser(
            final Supplier<ArgumentType<T>> argumentTypeSupplier,
            final @Nullable ParseFunction<T> parse
    ) {
        requireNonNull(argumentTypeSupplier, "brigadierType");
        this.nativeType = argumentTypeSupplier;
        this.parse = parse;
    }

    /**
     * Returns the backing Brigadier {@link ArgumentType} for this parser.
     *
     * @return the argument type
     * @since 1.5.0
     */
    public final ArgumentType<T> nativeArgumentType() {
        return this.nativeType.get();
    }

    @Override
    public final @NonNull ArgumentParseResult<@NonNull T> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        // Convert to a brig reader
        final StringReader reader = CloudStringReader.of(commandInput);

        // Then try to parse
        try {
            final T result = this.parse != null
                    ? this.parse.apply(this.nativeType.get(), reader)
                    : this.nativeType.get().parse(reader);
            return ArgumentParseResult.success(result);
        } catch (final CommandSyntaxException ex) {
            return ArgumentParseResult.failure(ex);
        }
    }

    @Override
    public final @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        /*
         * Strictly, this is incorrect.
         * However, it seems that all Mojang really does with the context passed here
         * is use it to query data on the native sender. Hopefully this hack holds up.
         */
        final com.mojang.brigadier.context.CommandContext<Object> reverseMappedContext = new com.mojang.brigadier.context.CommandContext<>(
                commandContext.getOrDefault(COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER, commandContext.sender()),
                input.input(),
                Collections.emptyMap(),
                null,
                null,
                Collections.emptyList(),
                StringRange.at(input.cursor()),
                null,
                null,
                false
        );

        return this.nativeType.get().listSuggestions(
                reverseMappedContext,
                new SuggestionsBuilder(input.input(), input.cursor())
        ).thenApply(suggestions -> {
            final List<Suggestion> cloud = new ArrayList<>();
            for (final com.mojang.brigadier.suggestion.Suggestion suggestion : suggestions.getList()) {
                final String beforeSuggestion = input.input().substring(input.cursor(), suggestion.getRange().getStart());
                final String afterSuggestion = input.input().substring(suggestion.getRange().getEnd());
                if (beforeSuggestion.isEmpty() && afterSuggestion.isEmpty()) {
                    cloud.add(TooltipSuggestion.suggestion(
                            suggestion.getText(),
                            suggestion.getTooltip()
                    ));
                } else {
                    cloud.add(TooltipSuggestion.suggestion(
                            beforeSuggestion + suggestion.getText() + afterSuggestion,
                            suggestion.getTooltip()
                    ));
                }
            }
            return cloud;
        });
    }

    /**
     * Function which can call {@link ArgumentType#parse(StringReader)} or another method.
     *
     * @param <T> result type
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    @FunctionalInterface
    public interface ParseFunction<T> {

        /**
         * Apply the parse function.
         *
         * @param type   argument type
         * @param reader string reader
         * @return result
         * @throws CommandSyntaxException on failure
         */
        T apply(ArgumentType<T> type, StringReader reader) throws CommandSyntaxException;
    }
}
