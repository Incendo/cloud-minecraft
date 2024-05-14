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
package org.incendo.cloud.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.type.tuple.Pair;

/**
 * Brigadier {@link Command} implementation that delegates to cloud.
 *
 * @param <C> command sender type
 * @param <S> brigadier command source type
 */
@API(status = API.Status.INTERNAL)
public final class CloudBrigadierCommand<C, S> implements Command<S> {

    private final CommandManager<C> commandManager;
    private final CloudBrigadierManager<C, S> brigadierManager;
    private final Function<String, String> inputMapper;

    /**
     * Creates a new {@link CloudBrigadierCommand}.
     *
     * @param commandManager   command manager
     * @param brigadierManager brigadier manager
     */
    public CloudBrigadierCommand(
        final @NonNull CommandManager<C> commandManager,
        final @NonNull CloudBrigadierManager<C, S> brigadierManager
    ) {
        this.commandManager = commandManager;
        this.brigadierManager = brigadierManager;
        this.inputMapper = Function.identity();
    }

    /**
     * Creates a new {@link CloudBrigadierCommand}.
     *
     * @param commandManager   command manager
     * @param brigadierManager brigadier manager
     * @param inputMapper      input mapper
     */
    public CloudBrigadierCommand(
        final @NonNull CommandManager<C> commandManager,
        final @NonNull CloudBrigadierManager<C, S> brigadierManager,
        final @NonNull Function<String, String> inputMapper
    ) {
        this.commandManager = commandManager;
        this.brigadierManager = brigadierManager;
        this.inputMapper = inputMapper;
    }

    @Override
    public int run(final @NonNull CommandContext<S> ctx) {
        final S source = ctx.getSource();
        final String input = this.inputMapper.apply(
            ctx.getInput().substring(parsedNodes(ctx.getLastChild()).get(0).second().getStart())
        );
        final C sender = this.brigadierManager.senderMapper().map(source);

        this.commandManager.commandExecutor().executeCommand(
            sender,
            input,
            cloudContext -> cloudContext.store(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER, source)
        );
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    /**
     * Return type changed at some point, but information is essentially the same. This code works for both versions of the
     * method.
     *
     * @param commandContext command context
     * @param <S>            source type
     * @return parsed nodes
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> List<Pair<com.mojang.brigadier.tree.CommandNode<S>, StringRange>> parsedNodes(
        final com.mojang.brigadier.context.CommandContext<S> commandContext
    ) {
        try {
            final Method getNodesMethod = commandContext.getClass().getDeclaredMethod("getNodes");
            final Object nodes = getNodesMethod.invoke(commandContext);
            if (nodes instanceof List) {
                return ParsedCommandNodeHandler.toPairList((List) nodes);
            } else if (nodes instanceof Map) {
                return ((Map<com.mojang.brigadier.tree.CommandNode<S>, StringRange>) nodes).entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            } else {
                throw new IllegalStateException();
            }
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }


    // Inner class to prevent attempting to load ParsedCommandNode when it doesn't exist
    @SuppressWarnings("unchecked")
    private static final class ParsedCommandNodeHandler {

        private ParsedCommandNodeHandler() {
        }

        private static <S> List<Pair<com.mojang.brigadier.tree.CommandNode<S>, StringRange>> toPairList(final List<?> nodes) {
            return ((List<ParsedCommandNode<S>>) nodes).stream()
                .map(n -> Pair.of(n.getNode(), n.getRange()))
                .collect(Collectors.toList());
        }
    }
}
