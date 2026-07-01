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

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandRegistrationHandler;

final class WaterdogPluginRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private final Map<CommandComponent<C>, dev.waterdog.waterdogpe.command.Command> registeredCommands = new HashMap<>();

    private WaterdogCommandManager<C> waterdogCommandManager;

    WaterdogPluginRegistrationHandler() {
    }

    void initialize(final @NonNull WaterdogCommandManager<C> waterdogCommandManager) {
        this.waterdogCommandManager = waterdogCommandManager;
    }

    @Override
    public boolean registerCommand(final @NonNull Command<C> command) {
        /* We only care about the root command argument */
        final CommandComponent<C> component = command.rootComponent();
        if (this.registeredCommands.containsKey(component)) {
            return false;
        }
        final WaterdogCommand<C> waterdogCommand = new WaterdogCommand<>(
                command,
                component,
                this.waterdogCommandManager
        );
        final boolean registered = this.waterdogCommandManager.owningPlugin().getProxy()
                .getCommandMap().registerCommand(waterdogCommand);
        if (registered) {
            this.registeredCommands.put(component, waterdogCommand);
        }
        return registered;
    }
}
