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

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.stream.Collectors;
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
import org.incendo.cloud.velocity.VelocityCaptionKeys;
import org.incendo.cloud.velocity.VelocityContextKeys;

/**
 * Argument parser for {@link RegisteredServer servers}
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public final class ServerParser<C> implements ArgumentParser<C, RegisteredServer>,
        BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, RegisteredServer> serverParser() {
        return ParserDescriptor.of(new ServerParser<>(), RegisteredServer.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #serverParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, RegisteredServer> serverComponent() {
        return CommandComponent.<C, RegisteredServer>builder().parser(serverParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull RegisteredServer> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        final RegisteredServer server = commandContext.get(VelocityContextKeys.PROXY_SERVER_KEY)
                .getServer(input)
                .orElse(null);
        if (server == null) {
            return ArgumentParseResult.failure(
                    new ServerParseException(
                            input,
                            commandContext
                    )
            );
        }
        return ArgumentParseResult.success(server);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return commandContext.get(VelocityContextKeys.PROXY_SERVER_KEY)
                .getAllServers()
                .stream()
                .map(s -> s.getServerInfo().getName())
                .collect(Collectors.toList());
    }

    public static final class ServerParseException extends ParserException {


        private ServerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    ServerParser.class,
                    context,
                    VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    CaptionVariable.of("input", input)
            );
        }
    }
}
