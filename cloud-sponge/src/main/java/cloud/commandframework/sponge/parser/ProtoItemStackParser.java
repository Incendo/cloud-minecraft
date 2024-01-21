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
import cloud.commandframework.sponge.data.ProtoItemStack;
import cloud.commandframework.sponge.exception.ComponentMessageRuntimeException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.util.ComponentMessageThrowable;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.nbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.persistence.NBTTranslator;

/**
 * An argument for parsing {@link ProtoItemStack ProtoItemStacks} from an {@link ItemType} identifier
 * and optional NBT data. The stack size of the resulting snapshot will always be {@code 1}.
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code apple}</li>
 *     <li>{@code minecraft:apple}</li>
 *     <li>{@code diamond_sword{Enchantments:[{id:sharpness,lvl:5}]}}</li>
 * </ul>
 *
 * @param <C> sender type
 */
public final class ProtoItemStackParser<C> implements NodeSource, ArgumentParser.FutureArgumentParser<C, ProtoItemStack>, SuggestionProvider<C> {

    public static <C> ParserDescriptor<C, ProtoItemStack> protoItemStackParser() {
        return ParserDescriptor.of(new ProtoItemStackParser<>(), ProtoItemStack.class);
    }

    private final ArgumentParser<C, ProtoItemStack> mappedParser =
        new WrappedBrigadierParser<C, ItemInput>(ItemArgument.item())
            .flatMapSuccess((ctx, itemInput) -> ArgumentParseResult.successFuture(new ProtoItemStackImpl(itemInput)));

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull ProtoItemStack>> parseFuture(
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
        return CommandTreeNodeTypes.ITEM_STACK.get().createNode();
    }

    private static final class ProtoItemStackImpl implements ProtoItemStack {

        // todo: use accessor
        private static final Field COMPOUND_TAG_FIELD =
            Arrays.stream(ItemInput.class.getDeclaredFields())
                .filter(f -> f.getType().equals(CompoundTag.class))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        static {
            COMPOUND_TAG_FIELD.setAccessible(true);
        }

        private final ItemInput itemInput;
        private final @Nullable DataContainer extraData;

        ProtoItemStackImpl(final @NonNull ItemInput itemInput) {
            this.itemInput = itemInput;
            try {
                final CompoundTag tag = (CompoundTag) COMPOUND_TAG_FIELD.get(itemInput);
                this.extraData = tag == null ? null : NBTTranslator.INSTANCE.translate(tag);
            } catch (final IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public @NonNull ItemType itemType() {
            return (ItemType) this.itemInput.getItem();
        }

        @Override
        public @Nullable DataContainer extraData() {
            return this.extraData;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public @NonNull ItemStack createItemStack(
            final int stackSize,
            final boolean respectMaximumStackSize
        ) throws ComponentMessageRuntimeException {
            try {
                return (ItemStack) (Object) this.itemInput.createItemStack(stackSize, respectMaximumStackSize);
            } catch (final CommandSyntaxException ex) {
                throw new ComponentMessageRuntimeException(ComponentMessageThrowable.getMessage(ex), ex);
            }
        }

        @Override
        public @NonNull ItemStackSnapshot createItemStackSnapshot(
            final int stackSize,
            final boolean respectMaximumStackSize
        ) throws ComponentMessageRuntimeException {
            return this.createItemStack(stackSize, respectMaximumStackSize).createSnapshot();
        }

    }

}
