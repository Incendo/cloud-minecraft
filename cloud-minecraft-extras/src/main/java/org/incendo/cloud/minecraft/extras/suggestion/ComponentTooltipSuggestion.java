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
package org.incendo.cloud.minecraft.extras.suggestion;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.incendo.cloud.internal.ImmutableImpl;
import org.incendo.cloud.suggestion.Suggestion;

@Value.Immutable
@ImmutableImpl
public interface ComponentTooltipSuggestion extends Suggestion {
    @Override
    @NonNull String suggestion();

    /**
     * Returns the {@link Component} tooltip or {@code null} if there is no tooltip.
     *
     * @return the tooltip
     */
    @Nullable Component tooltip();

    @Override
    @NonNull ComponentTooltipSuggestion withSuggestion(@NonNull String suggestion);

    /**
     * Returns a new {@link ComponentTooltipSuggestion} with no tooltip.
     *
     * @param suggestion suggestion
     * @return suggestion
     */
    static @NonNull ComponentTooltipSuggestion suggestion(final @NonNull String suggestion) {
        return ComponentTooltipSuggestionImpl.of(suggestion, null);
    }

    /**
     * Returns a new {@link ComponentTooltipSuggestion} with the provided tooltip.
     *
     * @param suggestion suggestion
     * @param tooltip    tooltip
     * @return suggestion
     */
    static @NonNull ComponentTooltipSuggestion suggestion(
        final @NonNull String suggestion,
        final @Nullable Component tooltip
    ) {
        return ComponentTooltipSuggestionImpl.of(suggestion, tooltip);
    }
}
