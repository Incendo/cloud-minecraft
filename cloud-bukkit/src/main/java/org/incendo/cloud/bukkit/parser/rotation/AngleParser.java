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
package org.incendo.cloud.bukkit.parser.rotation;

import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.FloatParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.type.range.Range;


/**
 * Parser that parsers a {@link Angle} from two floats.
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public final class AngleParser<C> implements ArgumentParser<C, Angle>, BlockingSuggestionProvider.Strings<C> {

    private static final Range<Integer> SUGGESTION_RANGE = Range.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * Creates a new angle parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Angle> angleParser() {
        return ParserDescriptor.of(new AngleParser<>(), Angle.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #angleParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Angle> angleComponent() {
        return CommandComponent.<C, Angle>builder().parser(angleParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Angle> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.skipWhitespace().peekString();

        final boolean relative;
        if (commandInput.peek() == '~') {
            relative = true;
            commandInput.moveCursor(1);
        } else {
            relative = false;
        }

        final float angle;
        try {
            final boolean empty = commandInput.peekString().isEmpty() || commandInput.peek() == ' ';
            angle = empty ? 0 : commandInput.readFloat();

            // You can have a prefix without a number, in which case we wouldn't consume the
            // subsequent whitespace. We do it manually.
            if (commandInput.hasRemainingInput() && commandInput.peek() == ' ') {
                commandInput.read();
            }
        } catch (final Exception e) {
            return ArgumentParseResult.failure(new FloatParser.FloatParseException(
                    input,
                    new FloatParser<>(
                            FloatParser.DEFAULT_MINIMUM,
                            FloatParser.DEFAULT_MAXIMUM
                    ),
                    commandContext
            ));
        }

        return ArgumentParseResult.success(
                Angle.of(
                        angle,
                        relative
                )
        );
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        String prefix;
        if (input.hasRemainingInput() && input.peek() == '~') {
            prefix = "~";
            input.moveCursor(1);
        } else {
            prefix = "";
        }

        return IntegerParser.getSuggestions(
                SUGGESTION_RANGE,
                input
        ).stream().map(string -> prefix + string).collect(Collectors.toList());
    }

}
