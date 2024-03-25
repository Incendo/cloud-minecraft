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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.annotations.AnnotationFeature;
import org.incendo.cloud.minecraft.signed.SignedString;

@DefaultQualifier(NonNull.class)
public final class SignedArgumentExample implements AnnotationFeature {
    private @MonotonicNonNull BukkitAudiences audiences;

    @Override
    public void registerFeature(final ExamplePlugin examplePlugin, final AnnotationParser<CommandSender> annotationParser) {
        this.audiences = examplePlugin.bukkitAudiences();
        annotationParser.parse(this);
    }

    @Command("annotations signed <message>")
    public void signedMessage(final CommandSender sender, final @Argument SignedString message) {
        final Audience audience = this.audiences.sender(sender);
        message.sendMessage(
            audience,
            ChatType.chatType(Key.key("paper", "raw")),
            MiniMessage.miniMessage().deserialize(message.string())
        );
    }
}
