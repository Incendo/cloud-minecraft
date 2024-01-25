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

import java.util.List;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.annotations.AnnotationFeature;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

/**
 * Example that uses an annotated command to query commands, and a `@Suggestions` annotated method to provide
 * suggestions for the help command.
 * <p>
 * This relies on {@link MinecraftHelp} from cloud-minecraft-extras.
 */
public final class HelpExample implements AnnotationFeature {

    private CommandManager<CommandSender> manager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private BukkitAudiences bukkitAudiences;


    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.manager = annotationParser.manager();
        this.bukkitAudiences = examplePlugin.bukkitAudiences();

        // Set up the help instance.
        this.setupHelp();

        // This will scan for `@Command` and `@Suggestions`.
        annotationParser.parse(this);
    }

    private void setupHelp() {
        this.minecraftHelp = MinecraftHelp.create(
                // The help command. This gets prefixed onto all the clickable queries.
                "/annotations help",
                // The command manager instance that is used to look up the commands.
                this.manager,
                // Tells the help manager how to map command senders to adventure audiences.
                this.bukkitAudiences::sender
        );
    }

    @Suggestions("help_queries")
    public @NonNull List<@NonNull String> suggestHelpQueries(
            final @NonNull CommandContext<CommandSender> ctx,
            final @NonNull String input
    ) {
        return this.manager.createHelpHandler()
                .queryRootIndex(ctx.sender())
                .entries()
                .stream()
                .map(CommandEntry::syntax)
                .toList();
    }

    @Command("annotations|an|a help [query]")
    @CommandDescription("Help menu")
    public void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument(value = "query", suggestions = "help_queries") @Greedy @Nullable String query
    ) {
        // MinecraftHelp looks up the relevant help pages and displays them to the sender.
        this.minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }
}
