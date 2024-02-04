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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;

@API(status = API.Status.INTERNAL)
final class PatternReplacingComponentCaptionFormatter<C> implements ComponentCaptionFormatter<C> {

    private final Pattern pattern;

    PatternReplacingComponentCaptionFormatter(final @NonNull Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public @NonNull Component formatCaption(
        final @NonNull Caption captionKey,
        final @NonNull C recipient,
        final @NonNull String caption,
        final @NonNull Collection<@NonNull CaptionVariable> variables
    ) {
        final Map<String, Component> replacements = new HashMap<>();
        for (final CaptionVariable variable : variables) {
            if (variable instanceof RichVariable) {
                replacements.put(variable.key(), ((RichVariable) variable).component());
            } else {
                replacements.put(variable.key(), Component.text(variable.value()));
            }
        }

        final TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
            .match(this.pattern)
            .replacement((matcher, builder) -> replacements.getOrDefault(matcher.group(1), Component.text(matcher.group())))
            .build();
        return Component.text(caption).replaceText(replacementConfig);
    }
}
