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
package org.incendo.cloud.minecraft.extras.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserContributor;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.type.tuple.Pair;

/**
 * Parser for color codes.
 * <p>
 * Accepts {@link NamedTextColor NamedTextColors}, Legacy Minecraft {@literal &} color codes, and Hex Codes (#RRGGBB or RRGGBB)
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class TextColorParser<C> implements ArgumentParser<C, TextColor>, BlockingSuggestionProvider.Strings<C> {

    private static final Pattern LEGACY_PREDICATE = Pattern.compile(
            "&[0-9a-fA-F]"
    );

    private static final Pattern HEX_PREDICATE = Pattern.compile(
            "#?([a-fA-F0-9]{1,6})"
    );

    private static final Collection<Pair<Character, NamedTextColor>> COLORS = Arrays.asList(
            Pair.of('0', NamedTextColor.BLACK),
            Pair.of('1', NamedTextColor.DARK_BLUE),
            Pair.of('2', NamedTextColor.DARK_GREEN),
            Pair.of('3', NamedTextColor.DARK_AQUA),
            Pair.of('4', NamedTextColor.DARK_RED),
            Pair.of('5', NamedTextColor.DARK_PURPLE),
            Pair.of('6', NamedTextColor.GOLD),
            Pair.of('7', NamedTextColor.GRAY),
            Pair.of('8', NamedTextColor.DARK_GRAY),
            Pair.of('9', NamedTextColor.BLUE),
            Pair.of('a', NamedTextColor.GREEN),
            Pair.of('b', NamedTextColor.AQUA),
            Pair.of('c', NamedTextColor.RED),
            Pair.of('d', NamedTextColor.LIGHT_PURPLE),
            Pair.of('e', NamedTextColor.YELLOW),
            Pair.of('f', NamedTextColor.WHITE)
    );

    /**
     * Creates a new text color parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, TextColor> textColorParser() {
        return ParserDescriptor.of(new TextColorParser<>(), TextColor.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #textColorParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, TextColor> textColorComponent() {
        return CommandComponent.<C, TextColor>builder().parser(textColorParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull TextColor> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        if (LEGACY_PREDICATE.matcher(input).matches()) {
            commandInput.moveCursor(1);
            final char code = Character.toLowerCase(commandInput.read());
            for (final Pair<Character, NamedTextColor> pair : COLORS) {
                if (pair.first() == code) {
                    return ArgumentParseResult.success(pair.second());
                }
            }
            // If we didn't match the input, we move back.
            commandInput.moveCursor(-2);
        }
        for (final Pair<Character, NamedTextColor> pair : COLORS) {
            if (pair.second().toString().equalsIgnoreCase(commandInput.peekString())) {
                commandInput.readString();
                return ArgumentParseResult.success(pair.second());
            }
        }
        if (HEX_PREDICATE.matcher(commandInput.peekString()).matches()) {
            if (commandInput.peek() == '#') {
                commandInput.moveCursor(1);
            }
            return ArgumentParseResult.success(TextColor.color(commandInput.readInteger(16)));
        }
        return ArgumentParseResult.failure(new TextColorParseException(commandContext, input));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input
    ) {
        final List<String> suggestions = new LinkedList<>();
        final String token = input.readString();
        final String tokenLower = token.toLowerCase(Locale.ROOT);
        boolean matchedName = false;
        for (final String name : NamedTextColor.NAMES.keys()) {
            if (name.contains(tokenLower)) {
                matchedName = true;
            }
            suggestions.add(name);
        }
        if (!matchedName && !token.isEmpty()) {
            if (token.equals("#")
                || (HEX_PREDICATE.matcher(token).matches() && (token.length() < (token.startsWith("#") ? 7 : 6)))) {
                for (char c = 'a'; c <= 'f'; c++) {
                    suggestions.add(String.format("%s%c", token, c));
                }
                for (char c = '0'; c <= '9'; c++) {
                    suggestions.add(String.format("%s%c", token, c));
                }
            }
        }
        return suggestions;
    }


    private static final class TextColorParseException extends ParserException {


        private TextColorParseException(
                final @NonNull CommandContext<?> commandContext,
                final @NonNull String input
        ) {
            super(
                    TextColorParser.class,
                    commandContext,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR,
                    CaptionVariable.of("input", input)
            );
        }
    }

    @API(status = API.Status.INTERNAL)
    public static final class Contributor implements ParserContributor {
        @Override
        public <C> void contribute(final ParserRegistry<C> registry) {
            try {
                registry.registerParser(textColorParser());
            } catch (final Exception | LinkageError ignore) {
                // ignore
            }
        }
    }
}
