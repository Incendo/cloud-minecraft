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
package org.incendo.cloud.cloudburst;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandRegistrationHandler;

class CloudburstPluginRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private final Map<CommandComponent<C>, org.cloudburstmc.server.command.Command> registeredCommands = new HashMap<>();

    private CloudburstCommandManager<C> cloudburstCommandManager;

    CloudburstPluginRegistrationHandler() {
    }

    void initialize(final @NonNull CloudburstCommandManager<C> cloudburstCommandManager) {
        this.cloudburstCommandManager = cloudburstCommandManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean registerCommand(final @NonNull Command<C> command) {
        /* We only care about the root command argument */
        final CommandComponent<C> component = command.rootComponent();
        if (this.registeredCommands.containsKey(component)) {
            return false;
        }
        final Plugin plugin = this.cloudburstCommandManager.owningPlugin();
        final CloudburstCommand<C> cloudburstCommand = new CloudburstCommand<>(
                component.name(),
                component.alternativeAliases(),
                command,
                component,
                this.cloudburstCommandManager
        );
        this.registeredCommands.put(component, cloudburstCommand);
        Server.getInstance().getCommandRegistry().register(plugin, cloudburstCommand);
        return true;
    }
}
