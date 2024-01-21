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
package cloud.commandframework.sponge;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.internal.CommandRegistrationHandler;
import io.leangen.geantyref.TypeToken;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

import static java.util.Objects.requireNonNull;

final class SpongeRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private SpongeCommandManager<C> commandManager;
    private final Set<cloud.commandframework.Command<C>> registeredCommands = new HashSet<>();

    SpongeRegistrationHandler() {
    }

    private void handleRegistrationEvent(final RegisterCommandEvent<Command.Raw> event) {
        this.commandManager.registrationCalled();
        for (final CommandNode<C> node : this.commandManager.commandTree().rootNodes()) {
            this.registerCommand(event, requireNonNull(node.component()));
        }
    }

    private void registerCommand(final RegisterCommandEvent<Command.Raw> event, final CommandComponent<C> rootLiteral) {
        final String label = rootLiteral.name();
        event.register(
                this.commandManager.owningPluginContainer(),
                new CloudSpongeCommand<>(label, this.commandManager),
                label,
                rootLiteral.alternativeAliases().toArray(new String[0])
        );
    }

    void initialize(final @NonNull SpongeCommandManager<C> commandManager) {
        this.commandManager = commandManager;
        Sponge.eventManager().registerListener(
                EventListenerRegistration.builder(new TypeToken<RegisterCommandEvent<Command.Raw>>() {})
                        .plugin(this.commandManager.owningPluginContainer())
                        .listener(this::handleRegistrationEvent)
                        .order(Order.DEFAULT)
                        .build()
        );
    }

    @Override
    public boolean registerCommand(final cloud.commandframework.@NonNull Command<C> command) {
        this.registeredCommands.add(command);
        return true;
    }
}
