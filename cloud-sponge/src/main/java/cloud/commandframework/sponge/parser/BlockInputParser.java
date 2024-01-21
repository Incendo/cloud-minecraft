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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.brigadier.parser.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.sponge.NodeSource;
import cloud.commandframework.sponge.data.BlockInput;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

/**
 * An argument for parsing {@link BlockInput} from a {@link BlockState}
 * and optional extra NBT data.
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code stone}</li>
 *     <li>{@code minecraft:stone}</li>
 *     <li>{@code andesite_stairs[waterlogged=true,facing=east]}</li>
 * </ul>
 *
 * @param <C> sender type
 */
public final class BlockInputParser<C> implements NodeSource, ArgumentParser.FutureArgumentParser<C, BlockInput>, SuggestionProvider<C> {

    public static <C> ParserDescriptor<C, BlockInput> blockInputParser() {
        return ParserDescriptor.of(new BlockInputParser<>(), BlockInput.class);
    }

    private final ArgumentParser<C, BlockInput> mappedParser =
        new WrappedBrigadierParser<C, net.minecraft.commands.arguments.blocks.BlockInput>(BlockStateArgument.block())
            .flatMapSuccess((ctx, blockInput) -> ArgumentParseResult.successFuture(new BlockInputImpl(blockInput)));

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull BlockInput>> parseFuture(
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
        return CommandTreeNodeTypes.BLOCK_STATE.get().createNode();
    }

    private static final class BlockInputImpl implements BlockInput {

        // todo: use accessor
        private static final Field COMPOUND_TAG_FIELD =
            Arrays.stream(net.minecraft.commands.arguments.blocks.BlockInput.class.getDeclaredFields())
                .filter(f -> f.getType().equals(CompoundTag.class))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        static {
            COMPOUND_TAG_FIELD.setAccessible(true);
        }

        private final net.minecraft.commands.arguments.blocks.BlockInput blockInput;
        private final @Nullable DataContainer extraData;

        BlockInputImpl(final net.minecraft.commands.arguments.blocks.@NonNull BlockInput blockInput) {
            this.blockInput = blockInput;
            try {
                final CompoundTag tag = (CompoundTag) COMPOUND_TAG_FIELD.get(blockInput);
                this.extraData = tag == null ? null : NBTTranslator.INSTANCE.translate(tag);
            } catch (final IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public @NonNull BlockState blockState() {
            return (BlockState) this.blockInput.getState();
        }

        @Override
        public @Nullable DataContainer extraData() {
            return this.extraData;
        }

        @Override
        public boolean place(final @NonNull ServerLocation location) {
            return this.place(location, BlockChangeFlags.DEFAULT_PLACEMENT);
        }

        @Override
        public boolean place(final @NonNull ServerLocation location, final @NonNull BlockChangeFlag flag) {
            return this.blockInput.place(
                (ServerLevel) location.world(),
                VecHelper.toBlockPos(location.position()),
                ((SpongeBlockChangeFlag) flag).getRawFlag()
            );
        }

    }

}
