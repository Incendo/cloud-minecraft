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
package org.incendo.cloud.minecraft.extras.caption;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionProvider;

public final class TranslatableCaption implements Caption {
    private final String key;

    public static Caption translatableCaption(final String key) {
        return new TranslatableCaption(key);
    }

    /**
     * Caption provider mapping {@link TranslatableCaption} directly to it's key, for use with
     * {@link ComponentCaptionFormatter#translatable()} or similar. This is necessary for the
     * {@link org.incendo.cloud.caption.CaptionRegistry} to recognize {@link TranslatableCaption}
     * without messages.
     *
     * @param <C> command sender type
     * @return caption provider
     */
    public static <C> CaptionProvider<C> translatableCaptionProvider() {
        return (caption, recipient) -> {
            if (caption instanceof TranslatableCaption) {
                return caption.key();
            }
            return null;
        };
    }

    public TranslatableCaption(final String key) {
        this.key = key;
    }

    @Override
    public @NonNull String key() {
        return this.key;
    }
}
