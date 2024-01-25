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
package org.incendo.cloud.examples.bukkit.annotations.feature;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.annotations.AnnotationFeature;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InjectionException;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

import static net.kyori.adventure.text.Component.text;

/**
 * Example that showcases parameter injection.
 */
public final class InjectionExample implements AnnotationFeature, ParameterInjector<CommandSender, GameMode> {

    private BukkitAudiences bukkitAudiences;

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.bukkitAudiences = examplePlugin.bukkitAudiences();

        annotationParser.manager().parameterInjectorRegistry().registerInjector(GameMode.class, this);
        // You can catch all exceptions thrown by the injection services.
        annotationParser.manager().exceptionController()
                // We need to unwrap the injection exception from the command execution exception.
                .registerHandler(CommandExecutionException.class, ExceptionHandler.unwrappingHandler(InjectionException.class))
                .registerHandler(
                        InjectionException.class,
                        ctx -> examplePlugin.bukkitAudiences().sender(ctx.context().sender())
                                .sendMessage(text("Injection failed: ", NamedTextColor.DARK_RED)
                                        .append(text(ctx.exception().getMessage(), NamedTextColor.DARK_RED)))
        );
        annotationParser.parse(this);
    }

    @Command("annotations whatismygamemode")
    public void gameModeCommand(
            final @NonNull CommandSender sender,
            final @NonNull GameMode gameMode
    ) {
        this.bukkitAudiences.sender(sender).sendMessage(text("Your game mode is: ", NamedTextColor.DARK_GREEN)
                .append(text(gameMode.name(), NamedTextColor.GREEN)));
    }

    @Override
    public @Nullable GameMode create(
            final @NonNull CommandContext<CommandSender> context,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        if (!(context.sender() instanceof Player)) {
            throw new IllegalArgumentException("Cannot get game mode for " + context.sender().getClass().getSimpleName());
        }
        return ((Player) context.sender()).getGameMode();
    }
}
