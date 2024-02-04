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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionVariable;

/**
 * Extension of {@link CaptionFormatter} to make it easier to convert from {@link Caption captions}
 * to adventure {@link Component components}.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ComponentCaptionFormatter<C> extends CaptionFormatter<C, Component> {

    /**
     * Create a MiniMessage formatter with the {@link MiniMessage#miniMessage() default MiniMessage instance} and
     * no extra {@link TagResolver TagResolvers}.
     *
     * @param <C> command sender type
     * @return MiniMessage formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> miniMessage() {
        return miniMessage(MiniMessage.miniMessage());
    }

    /**
     * Create a MiniMessage formatter with the provided MiniMessage instance and
     * no extra {@link TagResolver TagResolvers}.
     *
     * @param <C>         command sender type
     * @param miniMessage MiniMessage instance
     * @return MiniMessage formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> miniMessage(final @NonNull MiniMessage miniMessage) {
        return miniMessage(miniMessage, new TagResolver[]{});
    }

    /**
     * Create a MiniMessage formatter with the provided MiniMessage instance and
     * no extra {@link TagResolver TagResolvers}.
     *
     * @param <C>         command sender type
     * @param miniMessage MiniMessage instance
     * @param resolvers   extra resolvers
     * @return MiniMessage formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> miniMessage(
        final @NonNull MiniMessage miniMessage,
        final @NonNull TagResolver @NonNull... resolvers
    ) {
        return new MiniMessageComponentCaptionFormatter<>(miniMessage, Arrays.asList(resolvers));
    }

    /**
     * Caption formatter for {@link net.kyori.adventure.text.TranslatableComponent translatable components}.
     *
     * @param <C> command sender type
     * @return caption formatter
     */
    @API(status = API.Status.EXPERIMENTAL)
    static <C> @NonNull ComponentCaptionFormatter<C> translatable() {
        return new ComponentCaptionFormatter<C>() {
            @Override
            public @NonNull Component formatCaption(
                final @NonNull Caption captionKey,
                final @NonNull C recipient,
                final @NonNull String caption,
                final @NonNull List<@NonNull CaptionVariable> variables
            ) {
                return Component.translatable(captionKey.key(), variables.stream().map(variable -> {
                    if (variable instanceof RichVariable) {
                        return (RichVariable) variable;
                    } else {
                        return Component.text(variable.value());
                    }
                }).collect(Collectors.toList()));
            }
        };
    }

    /**
     * Returns a caption formatter that replaces the results from the given {@code pattern} with
     * the values from the caption variables.
     * <p>
     * The 1st capturing group will be used to determine the name of the variable to use for
     * the replacement.
     *
     * @param <C>     the command sender type
     * @param pattern the pattern
     * @return the formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> patternReplacing(
        final @NonNull Pattern pattern
    ) {
        return new PatternReplacingComponentCaptionFormatter<>(pattern);
    }

    /**
     * Returns a caption formatter that replaces placeholders in the form of {@code <placeholder>}
     * with the caption variables.
     *
     * @param <C> the command sender type
     * @return the formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> placeholderReplacing() {
        return new PatternReplacingComponentCaptionFormatter<>(CaptionFormatter.placeholderPattern());
    }

}
