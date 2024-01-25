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
package org.incendo.cloud.examples.bukkit.annotations.feature.minecraft;

import java.util.stream.Stream;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.annotations.AnnotationFeature;

/**
 * Example showcasing namespaced keys.
 */
public final class NamespacedKeyExample implements AnnotationFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        annotationParser.parse(this);
    }

    @Command("annotations namespaced <key> [rest]")
    public void namespacedKeyCommand(
            final @NonNull CommandSender sender,
            @Argument(value = "key", suggestions = "namespaced-suggestions") final @NonNull NamespacedKey key,
            @Argument("rest") @Greedy final @NonNull String rest
    ) {
        sender.sendMessage("The key is: " + key);
    }

    @Suggestions("namespaced-suggestions")
    public Stream<String> namespacedKeySuggestions(
            final @NonNull CommandContext<CommandSender> context,
            final @NonNull String input
    ) {
        return Stream.of("incendo:", "minecraft:", "")
                .flatMap(namespace -> Stream.of("cloud", "bee", "potato")
                        .map(key -> String.format("%s%s", namespace, key)));
    }
}
