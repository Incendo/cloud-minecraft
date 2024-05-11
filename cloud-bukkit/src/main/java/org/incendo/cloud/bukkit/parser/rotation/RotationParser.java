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
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.type.range.Range;

/**
 * Parser that parses a {@link Rotation} from two floats.
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public final class RotationParser<C> implements ArgumentParser<C, Rotation>, BlockingSuggestionProvider.Strings<C> {

    private static final Range<Integer> SUGGESTION_RANGE = Range.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

    private final AngleParser<C> angleParser = new AngleParser<>();

    /**
     * Creates a new rotation parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Rotation> rotationParser() {
        return ParserDescriptor.of(new RotationParser<>(), Rotation.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #rotationParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Rotation> rotationComponent() {
        return CommandComponent.<C, Rotation>builder().parser(rotationParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Rotation> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (commandInput.remainingTokens() < 2) {
            return ArgumentParseResult.failure(
                    new RotationParseException(
                            commandContext,
                            commandInput.remainingInput()
                    )
            );
        }

        Angle[] angles = new Angle[2];
        for (int i = 0; i < 2; i++) {
            if (commandInput.peekString().isEmpty()) {
                return ArgumentParseResult.failure(
                        new RotationParseException(
                                commandContext,
                                commandInput.remainingInput()
                        )
                );
            }

            final ArgumentParseResult<Angle> angle = this.angleParser.parse(
                    commandContext,
                    commandInput
            );
            if (angle.failure().isPresent()) {
                return ArgumentParseResult.failure(
                        angle.failure().get()
                );
            }

            angles[i] = angle.parsedValue().orElseThrow(NullPointerException::new);
        }

        return ArgumentParseResult.success(
                Rotation.of(
                        angles[0],
                        angles[1]
                )
        );
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        final int toSkip = Math.min(2, input.remainingTokens()) - 1;
        final StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < toSkip; i++) {
            prefix.append(input.readStringSkipWhitespace()).append(" ");
        }

        if (input.hasRemainingInput() && input.peek() == '~') {
            prefix.append(input.read());
        }

        return IntegerParser.getSuggestions(
                SUGGESTION_RANGE,
                input
        ).stream().map(string -> prefix + string).collect(Collectors.toList());
    }

    static class RotationParseException extends ParserException {

        protected RotationParseException(
                final @NonNull CommandContext<?> context,
                final @NonNull String input
        ) {
            super(
                    RotationParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_ROTATION_INVALID_FORMAT,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
