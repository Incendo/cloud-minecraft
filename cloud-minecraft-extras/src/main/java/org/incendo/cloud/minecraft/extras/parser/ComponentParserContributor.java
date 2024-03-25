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
package org.incendo.cloud.minecraft.extras.parser;

import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apiguardian.api.API;
import org.incendo.cloud.minecraft.extras.MinecraftExtrasParserParameters;
import org.incendo.cloud.minecraft.extras.annotation.specifier.Decoder;
import org.incendo.cloud.parser.ParserContributor;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.parser.StandardParameters;
import org.incendo.cloud.parser.standard.StringParser;

@API(status = API.Status.INTERNAL)
public final class ComponentParserContributor implements ParserContributor {

    @Override
    public <C> void contribute(final ParserRegistry<C> registry) {
        try {
            registry.registerAnnotationMapper(Decoder.MiniMessage.class, (annotation, parsedType) -> {
                return ParserParameters.single(MinecraftExtrasParserParameters.COMPONENT_DECODER, MiniMessage.miniMessage()::deserialize);
            });
            registry.registerAnnotationMapper(Decoder.Legacy.class, (annotation, parsedType) -> {
                final char character = annotation.value();
                return ParserParameters.single(
                    MinecraftExtrasParserParameters.COMPONENT_DECODER,
                    string -> LegacyComponentSerializer.legacy(character).deserialize(string)
                );
            });
            registry.registerAnnotationMapper(Decoder.Json.class, (annotation, parsedType) -> {
                final boolean downsampleColors = annotation.downsampleColors();
                final Function<String, ? extends Component> decoder =
                    downsampleColors ? GsonComponentSerializer.colorDownsamplingGson()::deserialize
                        : GsonComponentSerializer.gson()::deserialize;
                return ParserParameters.single(MinecraftExtrasParserParameters.COMPONENT_DECODER, decoder);
            });
            registry.registerAnnotationMapper(Decoder.class, (annotation, parsedType) -> {
                final Function<String, ? extends Component> decoder;
                try {
                    decoder = annotation.value().getConstructor().newInstance().decoder(parsedType);
                } catch (final ReflectiveOperationException exception) {
                    throw new IllegalArgumentException("Could not create decoder for " + annotation.value(), exception);
                }
                return ParserParameters.single(MinecraftExtrasParserParameters.COMPONENT_DECODER, decoder);
            });

            registry.registerParserSupplier(TypeToken.get(Component.class), options -> {
                final boolean greedy = options.get(StandardParameters.GREEDY, false);
                final boolean greedyFlagAware = options.get(StandardParameters.FLAG_YIELDING, false);
                final boolean quoted = options.get(StandardParameters.QUOTED, false);
                if (greedyFlagAware && quoted) {
                    throw new IllegalArgumentException(
                        "Don't know whether to create GREEDY_FLAG_YIELDING or QUOTED StringArgument.StringParser, both specified."
                    );
                } else if (greedy && quoted) {
                    throw new IllegalArgumentException(
                        "Don't know whether to create GREEDY or QUOTED StringArgument.StringParser, both specified."
                    );
                }
                final StringParser.StringMode stringMode;
                // allow @Greedy and @FlagYielding to both be true, give flag yielding priority
                if (greedyFlagAware) {
                    stringMode = StringParser.StringMode.GREEDY_FLAG_YIELDING;
                } else if (greedy) {
                    stringMode = StringParser.StringMode.GREEDY;
                } else if (quoted) {
                    stringMode = StringParser.StringMode.QUOTED;
                } else {
                    stringMode = StringParser.StringMode.SINGLE;
                }
                final Function<String, ? extends Component> decoder =
                    options.get(MinecraftExtrasParserParameters.COMPONENT_DECODER, MiniMessage.miniMessage()::deserialize);
                return new ComponentParser<>(decoder, stringMode);
            });
        } catch (final Exception | LinkageError ignore) {
            // ignore
        }
    }
}
