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
package org.incendo.cloud.examples.bukkit.builder.feature.minecraft;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;

import static org.incendo.cloud.bukkit.parser.NamespacedKeyParser.namespacedKeyParser;

/**
 * Example showcasing the namespaced key parser.
 */
public final class NamespacedKeyExample implements BuilderFeature {

    @Override
    public void registerFeature(
           final @NonNull ExamplePlugin examplePlugin,
           final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                    manager.commandBuilder("builder")
                    .literal("namespacedkey")
                    .required("key", namespacedKeyParser())
                    .handler(ctx -> {
                        final NamespacedKey namespacedKey = ctx.get("key");
                        final String key = namespacedKey.getNamespace() + ":" + namespacedKey.getKey();
                        ctx.sender().sendMessage("The key you typed is '" + key + "'.");
                    })
        );
    }
}
