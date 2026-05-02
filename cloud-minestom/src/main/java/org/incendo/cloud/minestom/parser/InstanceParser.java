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
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
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
 * Parser for online {@link Instance instances}, matched by UUID string.
 *
 * @param <C> command sender type
 */
public final class InstanceParser<C> implements ArgumentParser<C, Instance>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new instance parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Instance> instanceParser() {
        return ParserDescriptor.of(new InstanceParser<>(), Instance.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #instanceParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Instance> instanceComponent() {
        return CommandComponent.<C, Instance>builder().parser(instanceParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Instance> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        return MinecraftServer.getInstanceManager().getInstances().stream()
            .filter(i -> i.getUuid().toString().equals(input))
            .findFirst()
            .map(ArgumentParseResult::success)
            .orElseGet(() -> ArgumentParseResult.failure(new InstanceParseException(input, commandContext)));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return MinecraftServer.getInstanceManager().getInstances().stream()
            .map(i -> i.getUuid().toString())
            .collect(Collectors.toList());
    }

    /**
     * Exception thrown when an instance cannot be found for the input provided.
     */
    public static final class InstanceParseException extends ParserException {

        private final String input;

        /**
         * Create a new instance parse exception.
         *
         * @param input   string input
         * @param context command context
         */
        public InstanceParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                InstanceParser.class,
                context,
                MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_INSTANCE,
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
