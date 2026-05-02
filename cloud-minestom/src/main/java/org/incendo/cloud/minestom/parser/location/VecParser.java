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
package org.incendo.cloud.minestom.parser.location;

import java.util.List;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
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
 * Parser for {@link Vec} - parses the x, y, and z components.
 * <p>
 * Can be absolute components (e.g. {@code 100}) or relative (e.g. {@code ~} or {@code ~5}).
 * <p>
 * Non-entity senders using relative components will default to {@code 0}.
 *
 * @param <C> command sender type
 */
public final class VecParser<C> implements ArgumentParser<C, Vec>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new vec parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Vec> vecParser() {
        return ParserDescriptor.of(new VecParser<>(), Vec.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #vecParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Vec> vecComponent() {
        return CommandComponent.<C, Vec>builder().parser(vecParser());
    }

    private final LocationCoordinate<C> coordinateParser = LocationCoordinate.of(LocationCoordinateType.ABSOLUTE, 0);

    @Override
    public @NonNull ArgumentParseResult<@NonNull Vec> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        if (commandInput.remainingTokens() < 3) {
            return ArgumentParseResult.failure(
                new VecParseException(commandInput.remainingInput(), commandContext)
            );
        }

        @SuppressWarnings("unchecked")
        final LocationCoordinate<C>[] components = (LocationCoordinate<C>[]) new LocationCoordinate<?>[3];
        for (int i = 0; i < 3; i++) {
            if (commandInput.peekString().isEmpty()) {
                return ArgumentParseResult.failure(
                    new VecParseException(commandInput.remainingInput(), commandContext)
                );
            }
            final ArgumentParseResult<LocationCoordinate<C>> result =
                this.coordinateParser.parse(commandContext, commandInput);
            if (result.failure().isPresent()) {
                return ArgumentParseResult.failure(result.failure().get());
            }
            components[i] = result.parsedValue().orElseThrow(NullPointerException::new);
        }

        final Vec origin = this.resolveOrigin(commandContext);
        return ArgumentParseResult.success(new Vec(
            components[0].resolve(origin.x()),
            components[1].resolve(origin.y()),
            components[2].resolve(origin.z())
        ));
    }

    private @NonNull Vec resolveOrigin(final @NonNull CommandContext<C> commandContext) {
        final Object sender = commandContext.sender();
        if (sender instanceof CommandSender && sender instanceof Entity entity) {
            final var pos = entity.getPosition();
            return new Vec(pos.x(), pos.y(), pos.z());
        }
        return Vec.ZERO;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return List.of("~ ~ ~", "0 0 0");
    }

    /**
     * Exception thrown when a {@link Vec} cannot be parsed from the input provided.
     */
    public static final class VecParseException extends ParserException {

        private final String input;

        /**
         * Create a new vec parse exception.
         *
         * @param input   string input
         * @param context command context
         */
        public VecParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                VecParser.class,
                context,
                MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_VEC,
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
