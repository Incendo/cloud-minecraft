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
import cloud.commandframework.sponge.data.GameProfileCollection;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.profile.SpongeGameProfile;

/**
 * Argument for parsing a {@link Collection} of {@link GameProfile GameProfiles} from a
 * {@link Selector}. A successfully parsed result will contain at least one element.
 *
 * @param <C> sender type
 */
public final class GameProfileCollectionParser<C> implements NodeSource, ArgumentParser.FutureArgumentParser<C, GameProfileCollection>, SuggestionProvider<C> {

    public static <C> ParserDescriptor<C, GameProfileCollection> gameProfileCollectionParser() {
        return ParserDescriptor.of(new GameProfileCollectionParser<>(), GameProfileCollection.class);
    }

    private final ArgumentParser<C, GameProfileCollection> mappedParser =
        new WrappedBrigadierParser<C, GameProfileArgument.Result>(
            net.minecraft.commands.arguments.GameProfileArgument.gameProfile()
        ).flatMapSuccess((ctx, argumentResult) -> {
            final Collection<com.mojang.authlib.GameProfile> profiles;
            try {
                profiles = argumentResult.getNames(
                    (CommandSourceStack) ctx.get(SpongeCommandContextKeys.COMMAND_CAUSE)
                );
            } catch (final CommandSyntaxException ex) {
                return ArgumentParseResult.failureFuture(ex);
            }
            final List<SpongeGameProfile> result = profiles.stream()
                .map(SpongeGameProfile::of).collect(Collectors.toList());
            return ArgumentParseResult.successFuture(new GameProfileCollectionImpl(Collections.unmodifiableCollection(result)));
        });

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull GameProfileCollection>> parseFuture(
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
        return CommandTreeNodeTypes.GAME_PROFILE.get().createNode();
    }


    @DefaultQualifier(NonNull.class)
    private static final class GameProfileCollectionImpl extends AbstractCollection<GameProfile>
        implements GameProfileCollection {

        private final Collection<GameProfile> backing;

        private GameProfileCollectionImpl(final Collection<GameProfile> backing) {
            this.backing = backing;
        }

        @Override
        public int size() {
            return this.backing.size();
        }

        @Override
        public Iterator<GameProfile> iterator() {
            return this.backing.iterator();
        }

        @Override
        public boolean add(final GameProfile gameProfile) {
            return this.backing.add(gameProfile);
        }

    }

}
