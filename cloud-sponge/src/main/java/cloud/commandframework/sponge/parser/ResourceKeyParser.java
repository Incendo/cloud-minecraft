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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.sponge.NodeSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;

/**
 * Argument for parsing {@link ResourceKey ResourceKeys}.
 *
 * @param <C> command sender type
 */
public final class ResourceKeyParser<C> implements NodeSource, ArgumentParser<C, ResourceKey> {

    /**
     * Creates a new {@link ResourceKeyParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, ResourceKey> resourceKeyParser() {
        return ParserDescriptor.of(new ResourceKeyParser<>(), ResourceKey.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ResourceKey> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
    ) {
        final String input = inputQueue.readString();
        final ResourceKey key = ResourceKeyUtil.resourceKey(input);
        if (key == null) {
            return ResourceKeyUtil.invalidResourceKey();
        }
        return ArgumentParseResult.success(key);
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        return CommandTreeNodeTypes.RESOURCE_LOCATION.get().createNode();
    }

}
