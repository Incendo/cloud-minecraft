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
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.sponge.NodeSource;
import cloud.commandframework.sponge.SpongeCaptionKeys;
import cloud.commandframework.sponge.SpongeCommandContextKeys;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.profile.SpongeGameProfile;

/**
 * Argument for parsing a single {@link GameProfile} from a {@link Selector}.
 *
 * @param <C> command sender type
 */
public final class GameProfileParser<C> implements ArgumentParser.FutureArgumentParser<C, GameProfile>, NodeSource, SuggestionProvider<C> {

    /**
     * Creates a new {@link GameProfileParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, GameProfile> gameProfileParser() {
        return ParserDescriptor.of(new GameProfileParser<>(), GameProfile.class);
    }

    private final ArgumentParser<C, GameProfile> mappedParser =
        new WrappedBrigadierParser<C, net.minecraft.commands.arguments.GameProfileArgument.Result>(
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
            if (profiles.size() > 1) {
                return ArgumentParseResult.failureFuture(new TooManyGameProfilesSelectedException(ctx));
            }
            final GameProfile profile = SpongeGameProfile.of(profiles.iterator().next());
            return ArgumentParseResult.successFuture(profile);
        });

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull GameProfile>> parseFuture(
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
        return CommandTreeNodeTypes.GAME_PROFILE.get().createNode();
    }


    /**
     * Exception thrown when too many game profiles are selected.
     */
    private static final class TooManyGameProfilesSelectedException extends ParserException {

        private static final long serialVersionUID = -2931411139985042222L;

        TooManyGameProfilesSelectedException(final @NonNull CommandContext<?> context) {
            super(
                GameProfileParser.class,
                context,
                SpongeCaptionKeys.ARGUMENT_PARSE_FAILURE_GAME_PROFILE_TOO_MANY_SELECTED
            );
        }

    }

}
