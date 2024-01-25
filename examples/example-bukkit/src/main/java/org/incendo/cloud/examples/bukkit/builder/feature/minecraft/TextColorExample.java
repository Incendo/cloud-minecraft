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

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;
import org.incendo.cloud.minecraft.extras.ImmutableMinecraftHelp;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.RichDescription;

import static net.kyori.adventure.text.Component.text;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.minecraft.extras.parser.TextColorParser.textColorParser;

/**
 * Example showcasing the text color parser from cloud-minecraft-extras.
 */
public final class TextColorExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                manager.commandBuilder("builder")
                        .commandDescription(
                            richDescription(text(
                                "Sets the color scheme for '/example help'",
                                NamedTextColor.GREEN
                            ))
                        )
                        .literal("helpcolors")
                        .required(
                                "primary", textColorParser(),
                                RichDescription.of(text("The primary color for the color scheme", Style.style(TextDecoration.ITALIC)))
                        )
                        .required(
                                "highlight", textColorParser(),
                                RichDescription.of(text("The primary color used to highlight commands and queries"))
                        )
                        .required(
                                "alternate_highlight",
                                textColorParser(),
                                RichDescription.of(text("The secondary color used to highlight commands and queries"))
                        ).required("text", textColorParser(), RichDescription.of(text("The color used for description text")))
                        .required("accent", textColorParser(), RichDescription.of(text("The color used for accents and symbols")))
                        .handler(c -> examplePlugin.minecraftHelp(
                                ImmutableMinecraftHelp.copyOf(examplePlugin.minecraftHelp()).withColors(
                                        MinecraftHelp.helpColors(
                                                c.get("primary"),
                                                c.get("highlight"),
                                                c.get("alternate_highlight"),
                                                c.get("text"),
                                                c.get("accent")
                                        )
                                )
                        ))
        );
    }
}
