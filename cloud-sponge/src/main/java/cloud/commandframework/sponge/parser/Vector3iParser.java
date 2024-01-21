//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.brigadier.parser.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.sponge.NodeSource;
import cloud.commandframework.sponge.SpongeCommandContextKeys;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

/**
 * Argument for parsing {@link Vector3i} from relative, absolute, or local coordinates.
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code ~ ~ ~}</li>
 *     <li>{@code 1 1 1}</li>
 *     <li>{@code ~0.5 ~-2 ~10}</li>
 *     <li>{@code ^1 ^ ^-5}</li>
 * </ul>
 *
 * @param <C> sender type
 */
public final class Vector3iParser<C> implements NodeSource, ArgumentParser.FutureArgumentParser<C, Vector3i>, SuggestionProvider<C> {

    public static <C> ParserDescriptor<C, Vector3i> vector3iParser() {
        return ParserDescriptor.of(new Vector3iParser<>(), Vector3i.class);
    }

    private final ArgumentParser<C, Vector3i> mappedParser =
        new WrappedBrigadierParser<C, Coordinates>(BlockPosArgument.blockPos())
            .flatMapSuccess((ctx, coordinates) -> {
                return ArgumentParseResult.successFuture(VecHelper.toVector3i(
                    coordinates.getBlockPos((CommandSourceStack) ctx.get(SpongeCommandContextKeys.COMMAND_CAUSE))
                ));
            });

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull Vector3i>> parseFuture(
        @NonNull final CommandContext<@NonNull C> commandContext,
        @NonNull final CommandInput inputQueue
    ) {
        return this.mappedParser.parseFuture(commandContext, inputQueue);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
        final @NonNull CommandContext<C> context,
        final @NonNull CommandInput input
    ) {
        return this.mappedParser.suggestionProvider().suggestionsFuture(context, input);
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        return CommandTreeNodeTypes.BLOCK_POS.get().createNode();
    }

}
