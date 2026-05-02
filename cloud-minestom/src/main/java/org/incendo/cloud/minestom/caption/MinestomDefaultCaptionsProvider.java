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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

/**
 * Provides the default captions for messages in cloud-minestom.
 *
 * @param <C> command sender type
 */
public final class MinestomDefaultCaptionsProvider<C> extends DelegatingCaptionProvider<C> {

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '<input>'";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_ENTITY_TYPE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_ENTITY_TYPE = "'<input>' is not a valid entity type";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_INSTANCE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_INSTANCE = "No instance found for input '<input>'";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_GAME_MODE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_GAME_MODE = "'<input>' is not a valid game mode";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_DIMENSION_TYPE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_DIMENSION_TYPE = "'<input>' is not a valid dimension type";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_VEC}
     */
    public static final String ARGUMENT_PARSE_FAILURE_VEC = "'<input>' is not a valid vec (expected: x y z)";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_POS}
     */
    public static final String ARGUMENT_PARSE_FAILURE_POS = "'<input>' is not a valid pos (expected: x y z, optionally with yaw and pitch)";

    private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, ARGUMENT_PARSE_FAILURE_PLAYER)
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_ENTITY_TYPE, ARGUMENT_PARSE_FAILURE_ENTITY_TYPE)
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_INSTANCE, ARGUMENT_PARSE_FAILURE_INSTANCE)
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_GAME_MODE, ARGUMENT_PARSE_FAILURE_GAME_MODE)
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_DIMENSION_TYPE, ARGUMENT_PARSE_FAILURE_DIMENSION_TYPE)
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_VEC, ARGUMENT_PARSE_FAILURE_VEC)
        .putCaption(MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_POS, ARGUMENT_PARSE_FAILURE_POS)
        .build();

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
