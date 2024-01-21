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
import cloud.commandframework.brigadier.parser.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.sponge.SpongeCommandContextKeys;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.math.vector.Vector2d;

/**
 * Argument for parsing {@link Vector2d} from relative, absolute, or local coordinates.
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code ~ ~}</li>
 *     <li>{@code 0.1 -0.5}</li>
 *     <li>{@code ~1 ~-2}</li>
 * </ul>
 *
 * @param <C> sender type
 */
public final class Vector2dParser<C> extends VectorParser<C, Vector2d> {

    public static <C> ParserDescriptor<C, Vector2d> vector2dParser() {
        return vector2dParser(false);
    }

    public static <C> ParserDescriptor<C, Vector2d> vector2dParser(final boolean centerIntegers) {
        return ParserDescriptor.of(new Vector2dParser<>(centerIntegers), Vector2d.class);
    }

    private final ArgumentParser<C, Vector2d> mappedParser;

    /**
     * Create a new {@link Vector2dParser}.
     *
     * @param centerIntegers whether to center integers to x.5
     */
    public Vector2dParser(final boolean centerIntegers) {
        super(centerIntegers);
        this.mappedParser = new WrappedBrigadierParser<C, Coordinates>(new Vec2Argument(centerIntegers))
            .flatMapSuccess((ctx, coordinates) -> {
                final Vec3 position = coordinates.getPosition(
                    (CommandSourceStack) ctx.get(SpongeCommandContextKeys.COMMAND_CAUSE)
                );
                return ArgumentParseResult.successFuture(new Vector2d(position.x, position.z));
            });
    }

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull Vector2d>> parseFuture(
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
        return CommandTreeNodeTypes.VEC2.get().createNode();
    }
}
