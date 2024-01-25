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
package org.incendo.cloud.bukkit.parser;

import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Parser type that parses into {@link OfflinePlayer}. This is not thread safe. This
 * may also result in a blocking request to get the UUID for the offline player.
 * <p>
 * Avoid using this type if possible.
 *
 * @param <C> Command sender type
 */
public final class OfflinePlayerParser<C> implements ArgumentParser<C, OfflinePlayer>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new offline player parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, OfflinePlayer> offlinePlayerParser() {
        return ParserDescriptor.of(new OfflinePlayerParser<>(), OfflinePlayer.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #offlinePlayerParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, OfflinePlayer> playerComponent() {
        return CommandComponent.<C, OfflinePlayer>builder().parser(offlinePlayerParser());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NonNull ArgumentParseResult<OfflinePlayer> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        if (input.length() > 16) {
            return ArgumentParseResult.failure(new OfflinePlayerParseException(input, commandContext));
        }

        final OfflinePlayer player;
        try {
            player = Bukkit.getOfflinePlayer(input);
        } catch (final Exception e) {
            return ArgumentParseResult.failure(new OfflinePlayerParseException(input, commandContext));
        }

        return ArgumentParseResult.success(player);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        final CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !(bukkit instanceof Player && !((Player) bukkit).canSee(player)))
                .map(Player::getName)
                .collect(Collectors.toList());
    }


    /**
     * OfflinePlayer parse exception
     */
    public static final class OfflinePlayerParseException extends ParserException {

        private final String input;

        /**
         * Construct a new OfflinePlayer parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public OfflinePlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    OfflinePlayerParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String input() {
            return this.input;
        }
    }
}
