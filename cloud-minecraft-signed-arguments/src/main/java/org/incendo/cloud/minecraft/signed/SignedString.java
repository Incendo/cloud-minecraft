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
package org.incendo.cloud.minecraft.signed;

import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface SignedString {
    /**
     * Returns the raw string message.
     *
     * @return raw string message
     */
    String string();

    /**
     * Returns the signed message if the argument was actually signed by the client, otherwise {@code null}.
     *
     * @return signed message
     */
    @Nullable SignedMessage signedMessage();

    /**
     * Sends the signed message with the provided unsigned content.
     *
     * <p>If the message is not signed, it will be sent as a system message.</p>
     *
     * @param audience audience
     * @param chatType chat type, for if the message is signed
     * @param unsigned unsigned content
     */
    void sendMessage(Audience audience, ChatType.Bound chatType, Component unsigned);

    /**
     * Sends the signed message with the provided unsigned content.
     *
     * <p>If the message is not signed, it will be sent as a system message.</p>
     *
     * @param audience audience
     * @param chatType chat type, for if the message is signed
     * @param unsigned unsigned content
     */
    default void sendMessage(final Audience audience, final ChatType chatType, final Component unsigned) {
        this.sendMessage(audience, chatType.bind(unsigned), unsigned);
    }

    /**
     * Creates a new unsigned string wrapping {@code message}.
     *
     * @param message raw string message
     * @return unsigned string
     */
    static SignedString unsigned(final String message) {
        return new Unsigned(message);
    }

    final class Unsigned implements SignedString {
        private final String string;

        private Unsigned(final String string) {
            this.string = Objects.requireNonNull(string, "string");
        }

        @Override
        public @Nullable SignedMessage signedMessage() {
            return null;
        }

        @Override
        public void sendMessage(final Audience audience, final ChatType.Bound chatType, final Component unsigned) {
            audience.sendMessage(unsigned);
        }

        @Override
        public String string() {
            return this.string;
        }
    }
}
