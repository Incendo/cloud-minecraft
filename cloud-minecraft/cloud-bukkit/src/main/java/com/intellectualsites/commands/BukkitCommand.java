//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands;

import com.intellectualsites.commands.arguments.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.List;

final class BukkitCommand<C extends com.intellectualsites.commands.sender.CommandSender>
        extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private final CommandArgument<C, ?> command;
    private final BukkitCommandManager<C> bukkitCommandManager;
    private final com.intellectualsites.commands.Command<C, BukkitCommandMeta> cloudCommand;

    BukkitCommand(@Nonnull final com.intellectualsites.commands.Command<C, BukkitCommandMeta> cloudCommand,
                  @Nonnull final CommandArgument<C, ?> command,
                  @Nonnull final BukkitCommandManager<C> bukkitCommandManager) {
        super(command.getName());
        this.command = command;
        this.bukkitCommandManager = bukkitCommandManager;
        this.cloudCommand = cloudCommand;
    }

    @Override
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : strings) {
            builder.append(" ").append(string);
        }
        this.bukkitCommandManager.executeCommand(this.bukkitCommandManager.getCommandSenderMapper().apply(commandSender),
                                                 builder.toString())
                                 .whenComplete(((commandResult, throwable) -> {
                                     if (throwable != null) {
                                         commandSender.sendMessage(ChatColor.RED + throwable.getMessage());
                                         commandSender.sendMessage(ChatColor.RED + throwable.getCause().getMessage());
                                         throwable.printStackTrace();
                                         throwable.getCause().printStackTrace();
                                     } else {
                                         // Do something...
                                         commandSender.sendMessage("All good!");
                                     }
                                 }));
        return true;
    }

    @Override
    public String getDescription() {
        return this.cloudCommand.getCommandMeta().getOrDefault("description", "");
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws
            IllegalArgumentException {
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : args) {
            builder.append(" ").append(string);
        }
        return this.bukkitCommandManager.suggest(this.bukkitCommandManager.getCommandSenderMapper().apply(sender),
                                                 builder.toString());
    }

    @Override
    public Plugin getPlugin() {
        return this.bukkitCommandManager.getOwningPlugin();
    }

}
