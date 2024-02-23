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
package org.incendo.cloud.brigadier.suggestion;

import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.incendo.cloud.internal.ImmutableImpl;
import org.incendo.cloud.suggestion.Suggestion;

/**
 * {@link Suggestion} that has an optional Brigadier {@link Message message} tooltip.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
@Value.Immutable
@ImmutableImpl
public interface TooltipSuggestion extends Suggestion {

    /**
     * Returns a new {@link TooltipSuggestion} with the given {@code suggestion} and {@code tooltip}.
     *
     * @param suggestion the suggestion
     * @param tooltip    the optional tooltip that is displayed when hovering over the suggestion
     * @return the suggestion instance
     */
    static @NonNull TooltipSuggestion suggestion(
        final @NonNull String suggestion,
        final @Nullable Message tooltip
    ) {
        return TooltipSuggestionImpl.of(suggestion, tooltip);
    }

    /**
     * Returns a new {@link TooltipSuggestion} that uses the given {@code suggestion} and has a {@code null} tooltip.
     *
     * @param suggestion the suggestion
     * @return the suggestion instance
     */
    static @NonNull TooltipSuggestion tooltipSuggestion(
        final @NonNull Suggestion suggestion
    ) {
        if (suggestion instanceof TooltipSuggestion) {
            return (TooltipSuggestion) suggestion;
        }
        return suggestion(suggestion.suggestion(), null /* tooltip */);
    }

    @Override
    @NonNull String suggestion();

    /**
     * Returns the tooltip.
     *
     * @return the tooltip, or {@code null}
     */
    @Nullable Message tooltip();

    @Override
    @NonNull TooltipSuggestion withSuggestion(@NonNull String suggestion);
}
