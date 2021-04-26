//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.sponge.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.sponge.NodeSupplyingArgumentParser;
import cloud.commandframework.sponge.SpongeCommandContextKeys;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public final class Vector3dArgument<C> extends VectorArgument<C, Vector3d> {

    private Vector3dArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final boolean centerIntegers,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(centerIntegers),
                defaultValue,
                Vector3d.class,
                centerIntegers,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new optional {@link Vector3dArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link Vector3dArgument}
     */
    public static <C> @NonNull Vector3dArgument<C> optional(final @NonNull String name) {
        return Vector3dArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new required {@link Vector3dArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link Vector3dArgument}
     */
    public static <C> @NonNull Vector3dArgument<C> of(final @NonNull String name) {
        return Vector3dArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link Vector3dArgument}.
     *
     * @param name           argument name
     * @param centerIntegers whether to center integers to x.5
     * @param <C>            sender type
     * @return a new {@link Vector3dArgument}
     */
    public static <C> @NonNull Vector3dArgument<C> optional(final @NonNull String name, final boolean centerIntegers) {
        return Vector3dArgument.<C>builder(name).asOptional().centerIntegers(centerIntegers).build();
    }

    /**
     * Create a new required {@link Vector3dArgument}.
     *
     * @param name           argument name
     * @param centerIntegers whether to center integers to x.5
     * @param <C>            sender type
     * @return a new {@link Vector3dArgument}
     */
    public static <C> @NonNull Vector3dArgument<C> of(final @NonNull String name, final boolean centerIntegers) {
        return Vector3dArgument.<C>builder(name).centerIntegers(centerIntegers).build();
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link Builder}
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    public static final class Parser<C> implements NodeSupplyingArgumentParser<C, Vector3d> {

        private final ArgumentParser<C, Vector3d> mappedParser;

        public Parser(final boolean centerIntegers) {
            this.mappedParser = new WrappedBrigadierParser<C, Coordinates>(new Vec3Argument(centerIntegers))
                    .map((ctx, coordinates) -> {
                        return ArgumentParseResult.success(VecHelper.toVector3d(
                                coordinates.getPosition((CommandSourceStack) ctx.get(SpongeCommandContextKeys.COMMAND_CAUSE_KEY))
                        ));
                    });
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Vector3d> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            return this.mappedParser.parse(commandContext, inputQueue);
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            return ClientCompletionKeys.VEC3.get().createNode();
        }

    }

    public static final class Builder<C> extends VectorArgumentBuilder<C, Vector3d, Builder<C>> {

        Builder(final @NonNull String name) {
            super(Vector3d.class, name);
        }

        @Override
        public @NonNull Vector3dArgument<C> build() {
            return new Vector3dArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.centerIntegers(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

}
