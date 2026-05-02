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
package org.incendo.cloud.minestom.parser;

import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.world.DimensionType;
import org.apiguardian.api.API;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.minestom.caption.MinestomCaptionKeys;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jspecify.annotations.NonNull;

/**
 * Parser for {@link DimensionType dimension types}, matched by namespaced key.
 *
 * @param <C> command sender type
 */
public final class DimensionTypeParser<C> implements ArgumentParser<C, DimensionType>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new dimension type parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, DimensionType> dimensionTypeParser() {
        return ParserDescriptor.of(new DimensionTypeParser<>(), DimensionType.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #dimensionTypeParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, DimensionType> dimensionTypeComponent() {
        return CommandComponent.<C, DimensionType>builder().parser(dimensionTypeParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull DimensionType> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        final Key key;
        try {
            key = Key.key(input);
        } catch (final Exception e) {
            return ArgumentParseResult.failure(new DimensionTypeParseException(input, commandContext));
        }
        final DimensionType type = MinecraftServer.getDimensionTypeRegistry().get(key);
        if (type == null) {
            return ArgumentParseResult.failure(new DimensionTypeParseException(input, commandContext));
        }
        return ArgumentParseResult.success(type);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return MinecraftServer.getDimensionTypeRegistry().keys().stream()
            .map(key -> key.key().asString())
            .collect(Collectors.toList());
    }

    /**
     * Exception thrown when a dimension type cannot be found for the input provided.
     */
    public static final class DimensionTypeParseException extends ParserException {

        private final String input;

        /**
         * Create a new dimension type parse exception.
         *
         * @param input   string input
         * @param context command context
         */
        public DimensionTypeParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                DimensionTypeParser.class,
                context,
                MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_DIMENSION_TYPE,
                CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Returns the supplied input.
         *
         * @return input value
         */
        public @NonNull String input() {
            return this.input;
        }
    }
}
