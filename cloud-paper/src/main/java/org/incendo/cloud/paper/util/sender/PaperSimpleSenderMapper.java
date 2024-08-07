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
package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.paper.PaperCommandManager;

/**
 * A simple {@link SenderMapper} implementation designed for use with {@link PaperCommandManager}.
 * Allows for easily utilizing cloud's {@link Command.Builder#senderType(Class)} utilities despite the
 * single {@link CommandSourceStack} implementation provided by Paper/Minecraft.
 *
 * <p>The {@link #map(CommandSourceStack)} implementation performs type dispatch based on the {@link CommandSourceStack#getSender() sender},
 * for example it will create a {@link PlayerSource} when {@link CommandSourceStack#getSender()} is a {@link Player}, and similar for
 * {@link ConsoleSource} and {@link EntitySource}. Any other specific sender types do not currently have special handling
 * and will fall back to a generic {@link Source} implementation.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class PaperSimpleSenderMapper implements SenderMapper<CommandSourceStack, Source> {

    /**
     * Create a new instance of {@link PaperSimpleSenderMapper}.
     *
     * @return a new instance of {@link PaperSimpleSenderMapper}
     */
    public static @NonNull PaperSimpleSenderMapper simpleSenderMapper() {
        return new PaperSimpleSenderMapper();
    }

    PaperSimpleSenderMapper() {
    }

    @Override
    public @NonNull Source map(final @NonNull CommandSourceStack base) {
        CommandSender commandSender = base.getSender();

        if (commandSender instanceof ConsoleCommandSender) {
            return new ConsoleSource(base);
        }

        if (commandSender instanceof Player) {
            return new PlayerSource(base);
        }

        if (commandSender instanceof Entity) {
            return new EntitySource(base);
        }

        return new GenericSource(base);
    }

    @Override
    public @NonNull CommandSourceStack reverse(final @NonNull Source mapped) {
        return mapped.stack();
    }
}
