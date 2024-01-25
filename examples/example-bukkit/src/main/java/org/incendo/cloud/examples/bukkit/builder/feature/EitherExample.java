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

import io.leangen.geantyref.TypeToken;
import java.util.UUID;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.UUIDParser;
import org.incendo.cloud.type.Either;

import static net.kyori.adventure.text.Component.text;

/**
 * Example of a command accepting {@link Either}.
 */
public final class EitherExample implements BuilderFeature {

    private static final CloudKey<Either<UUID, Player>> EITHER_KEY = CloudKey.of(
        "uuid",
        new TypeToken<Either<UUID, Player>>() {}
    );

    @Override
    public void registerFeature(final @NonNull ExamplePlugin examplePlugin, final @NonNull BukkitCommandManager<CommandSender> manager) {
        manager.command(
            manager.commandBuilder("builder")
                .literal("either")
                .required(EITHER_KEY, ArgumentParser.firstOf(UUIDParser.uuidParser(), PlayerParser.playerParser()))
                .handler(context -> {
                    final Either<UUID, Player> either = context.get(EITHER_KEY);
                    final UUID uuid = either.primaryOrMapFallback(Player::getUniqueId);
                    examplePlugin.bukkitAudiences()
                        .sender(context.sender())
                        .sendMessage(text("The UUID is: ", NamedTextColor.DARK_GREEN).append(text(uuid.toString(), NamedTextColor.GREEN)));
                })
        );
    }
}
