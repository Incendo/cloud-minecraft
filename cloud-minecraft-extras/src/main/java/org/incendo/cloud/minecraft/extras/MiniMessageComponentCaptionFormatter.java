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
package org.incendo.cloud.minecraft.extras;

import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.intellij.lang.annotations.Subst;

@API(status = API.Status.INTERNAL)
final class MiniMessageComponentCaptionFormatter<C> implements ComponentCaptionFormatter<C> {

    private final MiniMessage miniMessage;
    private final List<TagResolver> extraResolvers;

    MiniMessageComponentCaptionFormatter(
        final @NonNull MiniMessage miniMessage,
        final @NonNull List<TagResolver> extraResolvers
    ) {
        this.miniMessage = miniMessage;
        this.extraResolvers = extraResolvers;
    }

    @Override
    public @NonNull Component formatCaption(
        final @NonNull Caption captionKey,
        final @NonNull C recipient,
        final @NonNull String caption,
        final @NonNull Collection<@NonNull CaptionVariable> variables
    ) {
        final TagResolver.Builder builder = TagResolver.builder();
        builder.resolvers(this.extraResolvers);
        for (final CaptionVariable variable : variables) {
            @Subst("key") final String key = variable.key();
            builder.resolver(Placeholder.parsed(key, variable.value()));
        }
        return this.miniMessage.deserialize(caption, builder.build());
    }
}
