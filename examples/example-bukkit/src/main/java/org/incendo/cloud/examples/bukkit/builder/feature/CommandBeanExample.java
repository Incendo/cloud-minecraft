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
package org.incendo.cloud.examples.bukkit.builder.feature;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandBean;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.examples.bukkit.ExamplePlugin;
import org.incendo.cloud.examples.bukkit.builder.BuilderFeature;
import org.incendo.cloud.key.CloudKey;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

/**
 * Example of using {@link CommandBean command beans} to register a command.
 */
public final class CommandBeanExample extends CommandBean<CommandSender> implements BuilderFeature {

    private static final CloudKey<Integer> NUMBER_KEY = CloudKey.of("number", Integer.class);

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        // You may register a CommandFactory<C> directly. This will invoke CommandFactory#createCommands.
        // CommandBean is an opinionated implementation of CommandFactory that outputs a single command.
        manager.command(this);
    }

    @Override
    protected @NonNull CommandProperties properties() {
        return CommandProperties.commandProperties("builder");
    }

    @Override
    protected Command.@NonNull Builder<CommandSender> configure(final Command.@NonNull Builder<CommandSender> builder) {
        return builder.literal("bean").required(NUMBER_KEY, integerParser());
    }

    @Override
    public void execute(final @NonNull CommandContext<CommandSender> commandContext) {
        final CommandSender sender = commandContext.sender();
        final int number = commandContext.get(NUMBER_KEY);
        sender.sendMessage("The square root of your number is: " + Math.sqrt(number));
    }
}
