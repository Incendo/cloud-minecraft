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
package org.incendo.cloud.velocity.parser;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.velocity.VelocityCaptionKeys;

import static com.velocitypowered.api.command.VelocityBrigadierMessage.tooltip;
import static org.incendo.cloud.brigadier.suggestion.TooltipSuggestion.tooltipSuggestion;

/**
 * Argument parser for {@link Player players}
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public final class PlayerParser<C> implements ArgumentParser<C, Player>, BlockingSuggestionProvider<C> {

    /**
     * Creates a new player parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Player> playerParser() {
        return ParserDescriptor.of(new PlayerParser<>(), Player.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #playerParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Player> playerComponent() {
        return CommandComponent.<C, Player>builder().parser(playerParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Player> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        final Player player = commandContext.<ProxyServer>get("ProxyServer")
            .getPlayer(input)
            .orElse(null);
        if (player == null) {
            return ArgumentParseResult.failure(
                new PlayerParseException(
                    input,
                    commandContext
                )
            );
        }
        return ArgumentParseResult.success(player);
    }

    @Override
    public @NonNull Iterable<? extends @NonNull Suggestion> suggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return commandContext.<ProxyServer>get("ProxyServer").getAllPlayers().stream()
            .map(player -> tooltipSuggestion(player.getUsername(), tooltip(Component.text(player.getUniqueId().toString()))))
            .collect(Collectors.toList());
    }

    public static final class PlayerParseException extends ParserException {


        private PlayerParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                PlayerParser.class,
                context,
                VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                CaptionVariable.of("input", input)
            );
        }
    }
}
