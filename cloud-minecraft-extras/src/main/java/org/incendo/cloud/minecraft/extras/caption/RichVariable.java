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

import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.internal.ImmutableImpl;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link CaptionVariable} implementation that uses Adventure components.
 */
@ImmutableImpl
@Value.Immutable
@SuppressWarnings("immutables:subtype")
@API(status = API.Status.STABLE)
public interface RichVariable extends CaptionVariable, ComponentLike {

    /**
     * Creates a new rich variable.
     *
     * @param key       variable key
     * @param component variable value
     * @return the variable
     */
    static @NonNull RichVariable of(final @NonNull String key, final @NonNull Component component) {
        return RichVariableImpl.of(key, component);
    }

    @Override
    @NonNull String key();

    /**
     * Returns the variable value as a component.
     *
     * @return the component
     */
    @NonNull Component component();

    @Override
    default @NotNull Component asComponent() {
        return this.component();
    }

    @SuppressWarnings("deprecation")
    @Override
    default @NonNull String value() {
        return net.kyori.adventure.text.serializer.plain.PlainComponentSerializer.plain()
            .serialize(GlobalTranslator.render(this.component(), Locale.getDefault()));
    }
}
