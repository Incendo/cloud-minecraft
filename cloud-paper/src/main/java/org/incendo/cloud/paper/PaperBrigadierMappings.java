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
package org.incendo.cloud.paper;

import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.paper.parser.KeyedWorldParser;

/**
 * Brigadier mappings for cloud-paper parsers.
 *
 * <p>This is currently only used when the PaperBrigadierListener is in use, not when the CloudCommodoreManager
 * is in use on Paper. This is because all argument types registered here require Paper 1.15+ anyways.</p>
 */
@API(status = API.Status.INTERNAL)
final class PaperBrigadierMappings {

    private PaperBrigadierMappings() {
    }

    static <C> void register(final @NonNull BukkitBrigadierMapper<C> mapper) {
        final Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
        if (keyed != null && keyed.isAssignableFrom(World.class)) {
            mapper.mapSimpleNMS(new TypeToken<KeyedWorldParser<C>>() {}, "resource_location", true);
        }
    }
}
