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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter;
import org.incendo.cloud.minecraft.extras.caption.RichVariable;
import org.incendo.cloud.util.TypeUtils;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

/**
 * Creates and registers {@link org.incendo.cloud.exception.handling.ExceptionHandler ExceptionHandlers} that send a {@link Component} to the
 * command sender.
 *
 * @param <C> command sender type
 */
public final class MinecraftExceptionHandler<C> {

    private static final Component NULL = text("null");

    /**
     * The default {@link InvalidSyntaxException} handler.
     *
     * @param <C> sender type
     * @return {@link InvalidSyntaxException} handler function
     * @see #defaultInvalidSyntaxHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MessageFactory<C, InvalidSyntaxException> createDefaultInvalidSyntaxHandler() {
        return (formatter, ctx) -> text()
            .color(NamedTextColor.RED)
            .append(ctx.context().formatCaption(
                formatter,
                StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX,
                RichVariable.of("syntax", ComponentHelper.highlight(text(String.format("/%s", ctx.exception().correctSyntax()),
                    NamedTextColor.GRAY), NamedTextColor.WHITE))
            ));
    }

    /**
     * The default {@link InvalidCommandSenderException} handler.
     *
     * @param <C> sender type
     * @return {@link InvalidCommandSenderException} handler function
     * @see #defaultInvalidSenderHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MessageFactory<C, InvalidCommandSenderException> createDefaultInvalidSenderHandler() {
        return (formatter, ctx) -> {
            final boolean multiple = ctx.exception().requiredSenderTypes().size() > 1;
            final Component expected = multiple
                ? Component.join(
                    JoinConfiguration.commas(true),
                    ctx.exception().requiredSenderTypes().stream()
                        .map(TypeUtils::simpleName)
                        .map(name -> text(name, NamedTextColor.GRAY))
                        .collect(Collectors.toList())
                )
                : text(TypeUtils.simpleName(ctx.exception().requiredSenderTypes().iterator().next()), NamedTextColor.GRAY);
            return text()
                    .color(NamedTextColor.RED)
                    .append(ctx.context().formatCaption(
                            formatter,
                            multiple ? StandardCaptionKeys.EXCEPTION_INVALID_SENDER_LIST : StandardCaptionKeys.EXCEPTION_INVALID_SENDER,
                            RichVariable.of("actual", text(TypeUtils.simpleName(ctx.context().sender().getClass()), NamedTextColor.GRAY)),
                            RichVariable.of("expected", expected)
                    ));
        };
    }

    /**
     * The default {@link NoPermissionException} handler.
     *
     * @param <C> sender type
     * @return {@link NoPermissionException} handler function
     * @see #defaultNoPermissionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MessageFactory<C, NoPermissionException> createDefaultNoPermissionHandler() {
        return (formatter, ctx) -> text()
            .color(NamedTextColor.RED)
            .append(ctx.context().formatCaption(
                formatter,
                StandardCaptionKeys.EXCEPTION_NO_PERMISSION,
                CaptionVariable.of("permission", ctx.exception().permissionResult().permission().permissionString())
            ));
    }

    /**
     * The default {@link ArgumentParseException} handler.
     *
     * @param <C> sender type
     * @return {@link ArgumentParseException} handler function
     * @see #defaultArgumentParsingHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MessageFactory<C, ArgumentParseException> createDefaultArgumentParsingHandler() {
        return (formatter, ctx) -> text()
            .color(NamedTextColor.RED)
            .append(ctx.context().formatCaption(
                formatter,
                StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT,
                RichVariable.of("cause", getMessage(formatter, ctx.exception().getCause()).colorIfAbsent(NamedTextColor.GRAY))
            ));
    }

    /**
     * Default logger for {@link #createDefaultCommandExecutionHandler(Consumer)}, using
     * {@link Throwable#printStackTrace()} on the {@link CommandExecutionException}'s cause.
     *
     * @param <C> sender type
     * @return default logger
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Consumer<ExceptionContext<C, CommandExecutionException>> createDefaultCommandExecutionLogger() {
        return ctx -> ctx.exception().getCause().printStackTrace();
    }

    /**
     * The default {@link CommandExecutionException} handler, using {@link #createDefaultCommandExecutionLogger()}.
     *
     * @param <C> sender type
     * @return {@link CommandExecutionException} handler function
     * @see #defaultCommandExecutionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MessageFactory<C, CommandExecutionException> createDefaultCommandExecutionHandler() {
        return createDefaultCommandExecutionHandler(createDefaultCommandExecutionLogger());
    }

    /**
     * The default {@link CommandExecutionException} handler, with a specific logger.
     *
     * @param logger logger
     * @param <C>    sender type
     * @return {@link CommandExecutionException} handler function
     * @see #defaultCommandExecutionHandler(Consumer)
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MessageFactory<C, CommandExecutionException> createDefaultCommandExecutionHandler(
        final Consumer<ExceptionContext<C, CommandExecutionException>> logger
    ) {
        return (formatter, ctx) -> {
            logger.accept(ctx);
            final Throwable cause = ctx.exception().getCause();

            final StringWriter writer = new StringWriter();
            cause.printStackTrace(new PrintWriter(writer));
            final String stackTrace = writer.toString().replaceAll("\t", "    ");
            final HoverEvent<Component> hover = HoverEvent.showText(
                text()
                    .append(getMessage(formatter, cause))
                    .append(newline())
                    .append(text(stackTrace))
                    .append(newline())
                    .append(text(
                        "    Click to copy",
                        NamedTextColor.GRAY,
                        TextDecoration.ITALIC
                    ))
            );
            final ClickEvent click = ClickEvent.copyToClipboard(stackTrace);
            return text()
                .append(ctx.context().formatCaption(formatter, StandardCaptionKeys.EXCEPTION_UNEXPECTED))
                .color(NamedTextColor.RED)
                .hoverEvent(hover)
                .clickEvent(click);
        };
    }

    private final Map<Class<? extends Throwable>, MessageFactory<C, ?>> componentBuilders = new HashMap<>();
    private final AudienceProvider<C> audienceProvider;
    private Decorator<C> decorator = (formatter, ctx, msg) -> msg;
    private ComponentCaptionFormatter<C> captionFormatter = ComponentCaptionFormatter.placeholderReplacing();

    private MinecraftExceptionHandler(final AudienceProvider<C> audienceProvider) {
        this.audienceProvider = audienceProvider;
    }

    /**
     * Create a new {@link MinecraftExceptionHandler} using {@code audienceProvider}.
     *
     * @param audienceProvider audience provider
     * @param <C>              sender type
     * @return new {@link MinecraftExceptionHandler}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MinecraftExceptionHandler<C> create(final AudienceProvider<C> audienceProvider) {
        return new MinecraftExceptionHandler<>(audienceProvider);
    }

    /**
     * Create a new {@link MinecraftExceptionHandler} using {@link AudienceProvider#nativeAudience()}.
     *
     * @param <C> sender type
     * @return new {@link MinecraftExceptionHandler}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C extends Audience> MinecraftExceptionHandler<C> createNative() {
        return create(AudienceProvider.nativeAudience());
    }

    /**
     * Use the default {@link InvalidSyntaxException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultInvalidSyntaxHandler() {
        return this.handler(InvalidSyntaxException.class, createDefaultInvalidSyntaxHandler());
    }

    /**
     * Use the default {@link InvalidCommandSenderException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultInvalidSenderHandler() {
        return this.handler(InvalidCommandSenderException.class, createDefaultInvalidSenderHandler());
    }

    /**
     * Use the default {@link NoPermissionException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultNoPermissionHandler() {
        return this.handler(NoPermissionException.class, createDefaultNoPermissionHandler());
    }

    /**
     * Use the default {@link ArgumentParseException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultArgumentParsingHandler() {
        return this.handler(ArgumentParseException.class, createDefaultArgumentParsingHandler());
    }

    /**
     * Use the default {@link CommandExecutionException} handler with {@link #createDefaultCommandExecutionLogger()}.
     *
     * @return {@code this}
     * @see #defaultCommandExecutionHandler(Consumer)
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultCommandExecutionHandler() {
        return this.defaultCommandExecutionHandler(createDefaultCommandExecutionLogger());
    }

    /**
     * Use the default {@link CommandExecutionException} handler with a custom logger.
     *
     * @param logger logger
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultCommandExecutionHandler(
        final @NonNull Consumer<ExceptionContext<C, CommandExecutionException>> logger
    ) {
        return this.handler(CommandExecutionException.class, createDefaultCommandExecutionHandler(logger));
    }

    /**
     * Use all the default exception handlers.
     *
     * @return {@code this}
     * @see #defaultArgumentParsingHandler()
     * @see #defaultInvalidSenderHandler()
     * @see #defaultInvalidSyntaxHandler()
     * @see #defaultNoPermissionHandler()
     * @see #defaultCommandExecutionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultHandlers() {
        return this
            .defaultArgumentParsingHandler()
            .defaultInvalidSenderHandler()
            .defaultInvalidSyntaxHandler()
            .defaultNoPermissionHandler()
            .defaultCommandExecutionHandler();
    }

    /**
     * Sets the exception handler for the given {@code type}. A handler
     * can return {@code null} to indicate no message should be sent.
     *
     * @param <T>              exception type
     * @param type             the exception type to handle
     * @param componentFactory the factory that produces the components
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Throwable> @This @NonNull MinecraftExceptionHandler<C> handler(
        final @NonNull Class<T> type,
        final @NonNull MessageFactory<C, T> componentFactory
    ) {
        this.componentBuilders.put(type, componentFactory);
        return this;
    }

    /**
     * Sets the decorator that acts on a component before it's sent to the sender.
     *
     * @param decorator the component decorator
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> decorator(
        final @NonNull Decorator<C> decorator
    ) {
        this.decorator = decorator;
        return this;
    }

    /**
     * Sets the caption formatter that is responsible for turning {@link org.incendo.cloud.caption.Caption captions} into Adventure {@link Component components}.
     *
     * @param captionFormatter caption formatter
     * @return {@code this}
     */
    public @This @NonNull MinecraftExceptionHandler<C> captionFormatter(final @NonNull ComponentCaptionFormatter<C> captionFormatter) {
        this.captionFormatter = Objects.requireNonNull(captionFormatter, "captionFormatter");
        return this;
    }

    /**
     * Sets the decorator that acts on a component before it's sent to the sender.
     *
     * @param decorator the component decorator
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> decorator(
        final @NonNull Function<@NonNull Component, @NonNull ComponentLike> decorator
    ) {
        return this.decorator((formatter, ctx, message) -> decorator.apply(message));
    }

    /**
     * Registers configured handlers to the {@link org.incendo.cloud.exception.handling.ExceptionController}.
     *
     * @param manager the manager instance
     * @since 2.0.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @API(status = API.Status.STABLE, since = "2.0.0")
    public void registerTo(final @NonNull CommandManager<C> manager) {
        this.componentBuilders.forEach((type, formatter) -> {
            manager.exceptionController().registerHandler(type, ctx -> {
                final @Nullable ComponentLike message = formatter.message(this.captionFormatter, (ExceptionContext) ctx);
                if (message != null) {
                    this.audienceProvider.apply(ctx.context().sender()).sendMessage(
                        this.decorator.decorate(this.captionFormatter, ctx, message.asComponent()));
                }
            });
        });
    }

    private static <C> Component getMessage(final ComponentCaptionFormatter<C> formatter, final Throwable throwable) {
        if (throwable instanceof ParserException) {
            return ((ParserException) throwable).formatCaption(formatter);
        }
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }


    @FunctionalInterface
    public interface MessageFactory<C, T extends Throwable> {

        /**
         * Formats the exception info into a {@link Component} message, or {@code null} to send no message.
         *
         * @param formatter        formatter to create components with
         * @param exceptionContext exception context
         * @return message or {@code null}
         */
        @Nullable ComponentLike message(@NonNull ComponentCaptionFormatter<C> formatter, @NonNull ExceptionContext<C, T> exceptionContext);
    }

    @FunctionalInterface
    public interface Decorator<C> {

        /**
         * Decorates a message before sending.
         *
         * @param formatter        caption formatter
         * @param exceptionContext exception context
         * @param message          message
         * @return decorated message
         */
        @NonNull ComponentLike decorate(
            @NonNull ComponentCaptionFormatter<C> formatter,
            @NonNull ExceptionContext<C, ?> exceptionContext,
            @NonNull Component message
        );
    }
}
