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
package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Command;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import cloud.commandframework.types.Either;
import java.util.UUID;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;

/**
 * Example of a command accepting {@link Either}.
 */
public final class EitherExample implements AnnotationFeature {

    private BukkitAudiences bukkitAudiences;

    @Override
    public void registerFeature(
        final @NonNull ExamplePlugin examplePlugin,
        final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.bukkitAudiences = examplePlugin.bukkitAudiences();
        annotationParser.parse(this);
    }

    @Command("annotations either <uuid>")
    public void eitherCommand(final @NonNull CommandSender sender, final @NonNull Either<UUID, Player> uuid) {
        final UUID resolvedUuid = uuid.primary().orElseGet(() -> uuid.fallback().map(Player::getUniqueId).get());
        this.bukkitAudiences.sender(sender)
            .sendMessage(text("The UUID is: ", NamedTextColor.DARK_GREEN).append(text(resolvedUuid.toString(), NamedTextColor.GREEN)));
    }
}
