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
import cloud.commandframework.sponge.SpongeCommandContextKeys;
import cloud.commandframework.sponge.data.ItemStackPredicate;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * An argument for parsing {@link ItemStackPredicate ItemStackPredicates}.
 *
 * @param <C> command sender type
 */
public final class ItemStackPredicateParser<C> implements ArgumentParser.FutureArgumentParser<C, ItemStackPredicate>,
    NodeSource, SuggestionProvider<C> {

    /**
     * Creates a new {@link ItemStackPredicateParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, ItemStackPredicate> itemStackPredicateParser() {
        return ParserDescriptor.of(new ItemStackPredicateParser<>(), ItemStackPredicate.class);
    }

    private final ArgumentParser<C, ItemStackPredicate> mappedParser =
        new WrappedBrigadierParser<C, ItemPredicateArgument.Result>(
            ItemPredicateArgument.itemPredicate()
        ).flatMapSuccess((ctx, result) -> {
            final CommandSourceStack commandSourceStack =
                (CommandSourceStack) ctx.get(SpongeCommandContextKeys.COMMAND_CAUSE);
            try {
                final com.mojang.brigadier.context.CommandContext<CommandSourceStack> dummyContext =
                    createDummyContext(ctx, commandSourceStack);
                return ArgumentParseResult.successFuture(new ItemStackPredicateImpl(result.create(dummyContext)));
            } catch (final CommandSyntaxException ex) {
                return ArgumentParseResult.failureFuture(ex);
            }
        });

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull ItemStackPredicate>> parseFuture(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
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
        return CommandTreeNodeTypes.ITEM_PREDICATE.get().createNode();
    }

    private static final class ItemStackPredicateImpl implements ItemStackPredicate {

        private final Predicate<net.minecraft.world.item.ItemStack> predicate;

        ItemStackPredicateImpl(final @NonNull Predicate<net.minecraft.world.item.ItemStack> predicate) {
            this.predicate = predicate;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean test(final @NonNull ItemStack itemStack) {
            return this.predicate.test((net.minecraft.world.item.ItemStack) (Object) itemStack);
        }

    }

    private static <C> com.mojang.brigadier.context.@NonNull CommandContext<CommandSourceStack> createDummyContext(
        final @NonNull CommandContext<C> ctx,
        final @NonNull CommandSourceStack commandSourceStack
    ) {
        return new com.mojang.brigadier.context.CommandContext<>(
            commandSourceStack,
            ctx.rawInput().input(),
            Collections.emptyMap(),
            null,
            null,
            Collections.emptyList(),
            StringRange.at(0),
            null,
            null,
            false
        );
    }

}
