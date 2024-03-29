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
package org.incendo.cloud.bungee.parser;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bungee.BungeeCaptionKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

/**
 * Argument parser for {@link net.md_5.bungee.api.config.ServerInfo servers}
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public final class ServerParser<C> implements ArgumentParser<C, ServerInfo> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, ServerInfo> serverParser() {
        return ParserDescriptor.of(new ServerParser<>(), ServerInfo.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #serverParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, ServerInfo> serverComponent() {
        return CommandComponent.<C, ServerInfo>builder().parser(serverParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ServerInfo> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        final ServerInfo server = commandContext.<ProxyServer>get("ProxyServer").getServerInfo(input);
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

    public static final class ServerParseException extends ParserException {


        private ServerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    ServerParser.class,
                    context,
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    CaptionVariable.of("input", input)
            );
        }
    }
}
