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
package cloud.commandframework.bukkit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

/**
 * Provides the default captions for messages in cloud-bukkit.
 *
 * @param <C> command sender type
 */
public final class BukkitDefaultCaptionsProvider<C> extends DelegatingCaptionProvider<C> {

    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_ENCHANTMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_ENCHANTMENT = "'<input>' is not a valid enchantment";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_MATERIAL}
     */
    public static final String ARGUMENT_PARSE_FAILURE_MATERIAL = "'<input>' is not a valid material name";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = "No player found for input '<input>'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '<input>'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_WORLD}
     */
    public static final String ARGUMENT_PARSE_FAILURE_WORLD = "'<input>' is not a valid Minecraft world";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED =
        "Entity selector argument type not supported below Minecraft 1.13.";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT =
        "'<input>' is not a valid location. Required format is '<x> <y> <z>'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE =
        "Cannot mix local and absolute coordinates. (either all coordinates use '^' or none do)";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE =
        "Invalid namespace '<input>'. Must be [a-z0-9._-]";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY}
     */
    public static final String ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY =
        "Invalid key '<input>'. Must be [a-z0-9/._-]";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY}
     */
    public static final String ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE =
        "Invalid input '<input>', requires an explicit namespace.";

    private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
        .putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_ENCHANTMENT,
            ARGUMENT_PARSE_FAILURE_ENCHANTMENT
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_MATERIAL,
            ARGUMENT_PARSE_FAILURE_MATERIAL
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER,
            ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
            ARGUMENT_PARSE_FAILURE_PLAYER
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD,
            ARGUMENT_PARSE_FAILURE_WORLD
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED,
            ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT,
            ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE,
            ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE,
            ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY,
            ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY
        ).putCaption(
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE,
            ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE
        )
        .build();

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
