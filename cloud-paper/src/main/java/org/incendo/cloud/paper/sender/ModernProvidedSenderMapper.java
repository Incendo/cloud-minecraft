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
package org.incendo.cloud.paper.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

@SuppressWarnings("UnstableApiUsage")
public final class ModernProvidedSenderMapper implements SenderMapper<CommandSourceStack, Sender> {

    /**
     * Create a new instance of {@link ModernProvidedSenderMapper}.
     *
     * @return a new instance of {@link ModernProvidedSenderMapper}
     */
    public static @NonNull ModernProvidedSenderMapper providedSenderMapper() {
        return new ModernProvidedSenderMapper();
    }

    ModernProvidedSenderMapper() {
    }

    @Override
    public @NonNull Sender map(final @NonNull CommandSourceStack base) {
        CommandSender commandSender = base.getSender();

        if (commandSender instanceof ConsoleCommandSender) {
            return new ConsoleSender(base);
        }

        if (commandSender instanceof Player) {
            return new PlayerSender(base);
        }

        return new GenericSender(base);
    }

    @Override
    public @NonNull CommandSourceStack reverse(final @NonNull Sender mapped) {
        return mapped.commandSourceStack();
    }
}
