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
package org.incendo.cloud.examples.bukkit;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.examples.bukkit.annotations.AnnotationParserExample;
import org.incendo.cloud.examples.bukkit.builder.BuilderExample;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter;
import org.incendo.cloud.paper.PaperCommandManager;

import static net.kyori.adventure.text.Component.text;

/**
 * Example plugin class
 */
@SuppressWarnings("unused")
public final class ExamplePlugin extends JavaPlugin {

    private BukkitAudiences bukkitAudiences;
    private MinecraftHelp<CommandSender> minecraftHelp;

    @Override
    public void onEnable() {
        //
        // (1) The execution coordinator determines how commands are executed. The simple execution coordinator will
        //     run the command on the thread that is calling it. In the case of Bukkit, this is the primary server thread.
        //     It is possible to execute (and parse!) commands asynchronously by using the
        //     AsynchronousCommandExecutionCoordinator.
        // (2) This function maps the Bukkit CommandSender to your custom sender type and back. If you're not using a custom
        //     type, then SenderMapper.identity() maps CommandSender to itself.
        //
        final PaperCommandManager<CommandSender> manager = new PaperCommandManager<>(
                /* Owning plugin */ this,
                /* (1) */ ExecutionCoordinator.simpleCoordinator(),
                /* (2) */ SenderMapper.identity()
        );
        //
        // Configure based on capabilities
        //
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            // Register Brigadier mappings for rich completions
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            // Use Paper async completions API (see Javadoc for why we don't use this with Brigadier)
            manager.registerAsynchronousCompletions();
        }
        //
        // Create the Bukkit audiences that maps command senders to adventure audience. This is not needed
        // if you're using paper-api instead of Bukkit.
        //
        this.bukkitAudiences = BukkitAudiences.create(this);
        //
        // Override the default exception handlers and use the exception handlers from cloud-minecraft-extras instead.
        //
        MinecraftExceptionHandler.create(this.bukkitAudiences::sender)
                .defaultInvalidSyntaxHandler()
                .defaultInvalidSenderHandler()
                .defaultNoPermissionHandler()
                .defaultArgumentParsingHandler()
                .defaultCommandExecutionHandler()
                .decorator(
                        component -> text()
                                .append(text("[", NamedTextColor.DARK_GRAY))
                                .append(text("Example", NamedTextColor.GOLD))
                                .append(text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                )
                .registerTo(manager);
        //
        // Create a help instance which is used in TextColorExample and HelpExample.
        //
        this.minecraftHelp = MinecraftHelp.<CommandSender>builder()
            .commandManager(manager)
            .audienceProvider(this.bukkitAudiences()::sender)
            .commandPrefix("/builder help")
            .messageProvider(MinecraftHelp.captionMessageProvider(
                manager.captionRegistry(),
                ComponentCaptionFormatter.miniMessage()
            ))
            .build();
        manager.captionRegistry().registerProvider(MinecraftHelp.defaultCaptionsProvider());
        //
        // Create the annotation examples.
        //
        new AnnotationParserExample(this, manager);
        //
        // Create the builder examples.
        //
        new BuilderExample(this, manager);
    }

    /**
     * Returns the {@link BukkitAudiences} instance.
     *
     * @return audiences
     */
    public @NonNull BukkitAudiences bukkitAudiences() {
        return this.bukkitAudiences;
    }

    /**
     * Returns the {@link MinecraftHelp} instance.
     *
     * @return minecraft help
     */
    public @NonNull MinecraftHelp<CommandSender> minecraftHelp() {
        return this.minecraftHelp;
    }

    /**
     * Replaces the {@link MinecraftHelp} instance.
     *
     * @param minecraftHelp the new instance
     */
    public void minecraftHelp(final @NonNull MinecraftHelp<CommandSender> minecraftHelp) {
        this.minecraftHelp = minecraftHelp;
    }
}
