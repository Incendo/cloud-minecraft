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
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.arguments.DimensionArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;

/**
 * Argument for retrieving {@link ServerWorld ServerWorlds} from the {@link WorldManager} by their {@link ResourceKey}.
 *
 * @param <C> sender type
 */
public final class WorldParser<C> implements ArgumentParser<C, ServerWorld>, NodeSource, BlockingSuggestionProvider.Strings<C> {

    public static <C> ParserDescriptor<C, ServerWorld> worldParser() {
        return ParserDescriptor.of(new WorldParser<>(), ServerWorld.class);
    }

    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE;

    static {
        try {
            // ERROR_INVALID_VALUE (todo: use accessor)
            final Field errorInvalidValueField = Arrays.stream(DimensionArgument.class.getDeclaredFields())
                .filter(f -> f.getType().equals(DynamicCommandExceptionType.class))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
            errorInvalidValueField.setAccessible(true);
            ERROR_INVALID_VALUE = (DynamicCommandExceptionType) errorInvalidValueField.get(null);
        } catch (final Exception ex) {
            throw new RuntimeException("Couldn't access ERROR_INVALID_VALUE command exception type.", ex);
        }
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ServerWorld> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
    ) {
        final String input = inputQueue.readString();
        final ResourceKey key = ResourceKeyUtil.resourceKey(input);
        if (key == null) {
            return ResourceKeyUtil.invalidResourceKey();
        }
        final Optional<ServerWorld> entry = Sponge.server().worldManager().world(key);
        if (entry.isPresent()) {
            return ArgumentParseResult.success(entry.get());
        }
        return ArgumentParseResult.failure(ERROR_INVALID_VALUE.create(key));
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return Sponge.server().worldManager().worlds().stream().flatMap(world -> {
            if (!input.isEmpty() && world.key().namespace().equals(ResourceKey.MINECRAFT_NAMESPACE)) {
                return Stream.of(world.key().value(), world.key().asString());
            }
            return Stream.of(world.key().asString());
        }).collect(Collectors.toList());
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        return CommandTreeNodeTypes.RESOURCE_LOCATION.get().createNode().customCompletions();
    }

}
