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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentDecoder;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.StringParser;

/**
 * Parser for components.
 * <p>
 * Uses a provided {@link ComponentDecoder} to decode a string into a {@link Component}.
 *
 * @param <C> Command sender type
 * @param <O> Output component type
 * @since 2.0.0
 */
@SuppressWarnings("NonExtendableApiUsage")
public final class ComponentParser<C, O extends Component> implements ArgumentParser<C, O> {

    private final ComponentDecoder<String, O> componentDecoder;
    private final StringParser<C> stringParser;

    /**
     * Construct a new component parser.
     *
     * @param componentDecoder the component decoder to deserialize the component
     * @param stringMode       the string mode to use for the input for the component decoder
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public ComponentParser(final @NonNull ComponentDecoder<String, O> componentDecoder, final StringParser.@NonNull StringMode stringMode) {
        this.componentDecoder = componentDecoder;
        this.stringParser = new StringParser<>(stringMode);
    }

    /**
     * Create a new parser descriptor for the default MiniMessage serializer using the
     * {@link StringParser.StringMode#QUOTED} mode.
     *
     * @param <C> Command sender type
     * @return the parser descriptor
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Component> miniMessageParser() {
        return miniMessageParser(StringParser.StringMode.QUOTED);
    }

    /**
     * Create a new parser descriptor for the default MiniMessage serializer using the
     * provided string mode.
     *
     * @param stringMode the string mode to use
     * @param <C>        Command sender type
     * @return the parser descriptor
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Component> miniMessageParser(final StringParser.@NonNull StringMode stringMode) {
        return componentParser(MiniMessage.miniMessage(), Component.class, stringMode);
    }

    /**
     * Create a new parser descriptor for the provided component decoder using the
     * {@link StringParser.StringMode#QUOTED} mode.
     *
     * @param componentDecoder the component decoder
     * @param classOfO         the class of the output component
     * @param <C>              Command sender type
     * @param <O>              Output component type
     * @return the parser descriptor
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, O extends Component> @NonNull ParserDescriptor<C, O> componentParser(
        final @NonNull ComponentDecoder<String, O> componentDecoder,
        final @NonNull Class<O> classOfO
    ) {
        return componentParser(componentDecoder, classOfO, StringParser.StringMode.QUOTED);
    }

    /**
     * Create a new parser descriptor for the provided component decoder using the
     * provided string mode.
     *
     * @param componentDecoder the component decoder
     * @param classOfO         the class of the output component
     * @param stringMode       the string mode to use
     * @param <C>              Command sender type
     * @param <O>              Output component type
     * @return the parser descriptor
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, O extends Component> @NonNull ParserDescriptor<C, O> componentParser(
        final @NonNull ComponentDecoder<String, O> componentDecoder,
        final @NonNull Class<O> classOfO,
        final StringParser.@NonNull StringMode stringMode
    ) {
        return ParserDescriptor.of(new ComponentParser<>(componentDecoder, stringMode), classOfO);
    }

    /**
     * Create a new command component builder for the default MiniMessage serializer using the
     * {@link StringParser.StringMode#QUOTED} mode.
     *
     * @param <C> Command sender type
     * @return the command component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Component> miniMessageComponent() {
        return miniMessageComponent(StringParser.StringMode.QUOTED);
    }

    /**
     * Create a new command component builder for the default MiniMessage serializer using the
     * provided string mode.
     *
     * @param stringMode the string mode to use
     * @param <C>        Command sender type
     * @return the command component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Component> miniMessageComponent(
        final StringParser.@NonNull StringMode stringMode
    ) {
        return componentComponent(MiniMessage.miniMessage(), Component.class, stringMode);
    }

    /**
     * Create a new command component builder for the provided component decoder using the
     * {@link StringParser.StringMode#QUOTED} mode.
     *
     * @param componentDecoder the component decoder
     * @param classOfO         the class of the output component
     * @param <C>              Command sender type
     * @param <O>              Output component type
     * @return the command component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, O extends Component> CommandComponent.@NonNull Builder<C, O> componentComponent(
        final @NonNull ComponentDecoder<String, O> componentDecoder,
        final @NonNull Class<O> classOfO
    ) {
        return CommandComponent.<C, O>builder().parser(componentParser(componentDecoder, classOfO));
    }

    /**
     * Create a new command component builder for the provided component decoder using the
     * provided string mode.
     *
     * @param componentDecoder the component decoder
     * @param classOfO         the class of the output component
     * @param stringMode       the string mode to use
     * @param <C>              Command sender type
     * @param <O>              Output component type
     * @return the command component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, O extends Component> CommandComponent.@NonNull Builder<C, O> componentComponent(
        final @NonNull ComponentDecoder<String, O> componentDecoder,
        final @NonNull Class<O> classOfO,
        final StringParser.@NonNull StringMode stringMode
    ) {
        return CommandComponent.<C, O>builder().parser(componentParser(componentDecoder, classOfO, stringMode));
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull O> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        final ArgumentParseResult<String> result = this.stringParser.parse(commandContext, commandInput);
        if (result.failure().isPresent()) {
            return ArgumentParseResult.failure(result.failure().get());
        }
        try {
            //noinspection OptionalGetWithoutIsPresent
            return ArgumentParseResult.success(this.componentDecoder.deserialize(result.parsedValue().get()));
        } catch (final Exception exception) {
            return ArgumentParseResult.failure(exception);
        }
    }

    /**
     * Returns the string mode used by this parser.
     *
     * @return the string mode
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public StringParser.@NonNull StringMode stringMode() {
        return this.stringParser.stringMode();
    }

    /**
     * Returns the component decoder used by this parser.
     *
     * @return the component decoder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull ComponentDecoder<String, O> componentDecoder() {
        return this.componentDecoder;
    }
}
