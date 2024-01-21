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
package cloud.commandframework.sponge.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.sponge.NodeSource;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.parameter.managed.operator.Operator;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.registry.RegistryTypes;

/**
 * An argument for parsing {@link Operator Operators}.
 *
 * @param <C> command sender type
 */
public final class OperatorParser<C> implements NodeSource, ArgumentParser<C, Operator>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new {@link OperatorParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, Operator> operatorParser() {
        return ParserDescriptor.of(new OperatorParser<>(), Operator.class);
    }

    private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION;

    static {
        try {
            // todo: fix in a better way
            final Class<?> spongeAccessor =
                Class.forName("org.spongepowered.common.accessor.commands.arguments.OperationArgumentAccessor");
            final Method get = spongeAccessor.getDeclaredMethod("accessor$ERROR_INVALID_OPERATION");
            get.setAccessible(true);
            ERROR_INVALID_OPERATION = (SimpleCommandExceptionType) get.invoke(null);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException("Couldn't access ERROR_INVALID_OPERATION command exception type.", ex);
        }
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Operator> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
    ) {
        final String input = inputQueue.readString();
        final Optional<Operator> operator = RegistryTypes.OPERATOR.get().stream()
            .filter(op -> op.asString().equals(input))
            .findFirst();
        if (!operator.isPresent()) {
            return ArgumentParseResult.failure(ERROR_INVALID_OPERATION.create());
        }
        return ArgumentParseResult.success(operator.get());
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return RegistryTypes.OPERATOR.get().stream()
            .map(Operator::asString)
            .collect(Collectors.toList());
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        return CommandTreeNodeTypes.OPERATION.get().createNode();
    }

}
