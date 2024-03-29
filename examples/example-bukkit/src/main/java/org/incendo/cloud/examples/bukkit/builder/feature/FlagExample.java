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
package org.incendo.cloud.examples.bukkit.builder.feature;

import java.util.Locale;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;

import static org.incendo.cloud.bukkit.parser.EnchantmentParser.enchantmentParser;
import static org.incendo.cloud.bukkit.parser.MaterialParser.materialParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

/**
 * Example that showcases command flags with values.
 */
public final class FlagExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                manager.commandBuilder("builder")
                        .literal("give")
                        .required("material", materialParser())
                        .required("amount", integerParser(0 /* minValue */))
                        .flag(manager.flagBuilder("color").withComponent(enumParser(ChatColor.class)))
                        .flag(manager.flagBuilder("enchant").withComponent(enchantmentParser()))
                        .senderType(Player.class)
                        .handler(context -> {
                            final ItemStack itemStack = new ItemStack(
                                    context.get("material"),
                                    context.get("amount")
                            );
                            String itemName = String.format(
                                    "%s's %s",
                                    context.sender().getName(),
                                    context.<Material>get("material").name()
                                            .toLowerCase(Locale.ROOT)
                                            .replace('_', ' ')
                            );
                            if (context.flags().contains("color")) {
                                final ChatColor color = context.flags().get("color");
                                itemName = color + itemName;
                            }
                            final ItemMeta meta = itemStack.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName(itemName);
                                itemStack.setItemMeta(meta);
                            }
                            if (context.flags().contains("enchant")) {
                                final Enchantment enchantment = context.flags().get("enchant");
                                itemStack.addUnsafeEnchantment(Objects.requireNonNull(enchantment), 10);
                            }
                            context.sender().getInventory().addItem(itemStack);
                            context.sender().sendMessage(
                                    ChatColor.GREEN + String.format(
                                            "You have been given %d x %s",
                                            context.<Integer>get("amount"),
                                            context.<Material>get("material")
                                    )
                            );
                        })
        );
    }
}
