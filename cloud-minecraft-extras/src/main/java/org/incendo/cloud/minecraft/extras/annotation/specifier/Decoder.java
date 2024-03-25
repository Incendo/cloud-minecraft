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
package org.incendo.cloud.minecraft.extras.annotation.specifier;

import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.apiguardian.api.API;

/**
 * Annotation used to specify a decoder for converting a string
 * into a component.
 * @since 2.0.0
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@API(status = API.Status.STABLE, since = "2.0.0")
public @interface Decoder {

    /**
     * The class for a provider of a decoder.
     *
     * @return The class for a provider of a decoder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    Class<? extends Provider> value();

    /**
     * Annotation to specify to use a MiniMessage decoder.
     * @since 2.0.0
     */
    @Documented
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @API(status = API.Status.STABLE, since = "2.0.0")
    @interface MiniMessage {
    }

    /**
     * Annotation to specify to use a legacy decoder.
     * @since 2.0.0
     */
    @Documented
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @API(status = API.Status.STABLE, since = "2.0.0")
    @interface Legacy {

        /**
         * The character to use for legacy formatting.
         *
         * @return The character to use for legacy formatting
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        char value() default '&';
    }

    /**
     * Annotation to specify to use a json decoder.
     * @since 2.0.0
     */
    @Documented
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @API(status = API.Status.STABLE, since = "2.0.0")
    @interface Json {

        /**
         * Whether to downsample colors.
         *
         * @return Whether to downsample colors
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        boolean downsampleColors() default false;
    }

    /**
     * A provider of a decoder.
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface Provider {

        /**
         * Returns a decoder function to convert a string into
         * a component.
         *
         * @param parsedType The parsed type
         * @return The decoder function
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        Function<String, ? extends Component> decoder(TypeToken<?> parsedType);
    }
}
