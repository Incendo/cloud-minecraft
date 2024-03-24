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

import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.parser.ParserParameter;

/**
 * {@link ParserParameter} keys for cloud-minecraft-extras
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public final class MinecraftExtraParserParameters {

    private MinecraftExtraParserParameters() {
    }

    /**
     * Used to specify a {@link Function} that decodes a string into a {@link Component} for
     * {@link org.incendo.cloud.minecraft.extras.parser.ComponentParser}.
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static final ParserParameter<Function<String, ? extends Component>> COMPONENT_DECODER =
        create("component_decoder", new TypeToken<Function<String, ? extends Component>>() {});

    private static <T> @NonNull ParserParameter<T> create(
        final @NonNull String key,
        final @NonNull TypeToken<T> expectedType
    ) {
        return new ParserParameter<>(key, expectedType);
    }
}
