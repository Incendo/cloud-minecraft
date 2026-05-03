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
package org.incendo.cloud.minestom.caption;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;

/**
 * {@link Caption} instances for messages in cloud-minestom
 */
public final class MinestomCaptionKeys {

    private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_PLAYER = of("argument.parse.failure.player");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_ENTITY_TYPE = of("argument.parse.failure.entity_type");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_INSTANCE = of("argument.parse.failure.instance");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_GAME_MODE = of("argument.parse.failure.game_mode");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_DIMENSION_TYPE = of("argument.parse.failure.dimension_type");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_VEC = of("argument.parse.failure.vec");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_POS = of("argument.parse.failure.pos");

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_ITEM_STACK = of("argument.parse.failure.item_stack");

    private MinestomCaptionKeys() {
    }

    private static @NonNull Caption of(final @NonNull String key) {
        final Caption caption = Caption.of(key);
        RECOGNIZED_CAPTIONS.add(caption);
        return caption;
    }

    /**
     * Returns an immutable collection containing all standard caption keys.
     *
     * @return immutable collection of keys
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull Collection<@NonNull Caption> minestomCaptionKeys() {
        return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
    }
}
