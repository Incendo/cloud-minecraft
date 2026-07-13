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
package org.incendo.cloud.waterdog;

import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;

final class WaterdogCommand<C> extends Command {

    private final WaterdogCommandManager<C> manager;
    private final CommandComponent<C> command;

    WaterdogCommand(
            final org.incendo.cloud.@NonNull Command<C> cloudCommand,
            final @NonNull CommandComponent<C> command,
            final @NonNull WaterdogCommandManager<C> manager
    ) {
        super(
                command.name(),
                CommandSettings.builder()
                        .setAliases(command.alternativeAliases().toArray(new String[0]))
                        .setDescription(cloudCommand.commandDescription().description().textDescription())
                        .setPermission(cloudCommand.commandPermission().permissionString())
                        .build()
        );
        this.command = command;
        this.manager = manager;
    }

    @Override
    public boolean onExecute(final CommandSender sender, final @Nullable String alias, final String[] args) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.name());
        for (final String arg : args) {
            builder.append(" ").append(arg);
        }
        final C cloudSender = this.manager.senderMapper().map(sender);
        this.manager.commandExecutor().executeCommand(cloudSender, builder.toString());
        return true;
    }
}
