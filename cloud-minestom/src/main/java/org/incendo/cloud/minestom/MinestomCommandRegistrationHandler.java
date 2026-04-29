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
package org.incendo.cloud.minestom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.StringUtils;

final class MinestomCommandRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private static final net.minestom.server.command.builder.CommandExecutor EMPTY_EXECUTOR =
        (s, c) -> {};

    private final Map<String, MinestomCommand<C>> registeredCommands = new HashMap<>();

    private MinestomCommandManager<C> manager;

    MinestomCommandRegistrationHandler() {}

    void initialize(final @NonNull MinestomCommandManager<C> manager) {
        this.manager = manager;
    }

    @Override
    public boolean registerCommand(final @NonNull Command<C> command) {
        final CommandComponent<C> rootComponent = command.rootComponent();
        final String rootName = rootComponent.name();

        MinestomCommand<C> minestomCommand = this.registeredCommands.get(rootName);
        if (minestomCommand == null) {
            final String[] aliases = rootComponent.alternativeAliases().toArray(new String[0]);

            minestomCommand = new MinestomCommand<>(rootName, aliases, this.manager);
            MinecraftServer.getCommandManager().register(minestomCommand);

            this.registeredCommands.put(rootName, minestomCommand);
        }

        final List<CommandComponent<C>> components = command.components();
        net.minestom.server.command.builder.Command parent = minestomCommand;
        final List<Argument<?>> minestomArgs = new ArrayList<>();
        boolean inLiteralChain = true;

        // skip root component (index 0)
        for (int i = 1; i < components.size(); i++) {
            final CommandComponent<C> component = components.get(i);

            if (inLiteralChain && component.type() == CommandComponent.ComponentType.LITERAL) {
                parent = this.getOrCreateSubcommand(parent, component);
            } else {
                inLiteralChain = false;
                final ArgumentWord arg = new ArgumentWord(component.name());
                this.addSuggestions(arg);
                minestomArgs.add(arg);
            }
        }

        parent.addSyntax(EMPTY_EXECUTOR, minestomArgs.toArray(new Argument<?>[0]));
        return true;
    }

    private net.minestom.server.command.builder.Command getOrCreateSubcommand(
        final net.minestom.server.command.builder.Command parent,
        final CommandComponent<C> component
    ) {
        final String name = component.name();
        final net.minestom.server.command.builder.Command existing = parent.getSubcommands().stream()
            .filter(sub -> sub.getName().equals(name))
            .findFirst()
            .orElse(null);

        if (existing != null) {
            return existing;
        }

        final String[] aliases = component.alternativeAliases().toArray(new String[0]);
        final net.minestom.server.command.builder.Command sub = new net.minestom.server.command.builder.Command(name, aliases);
        parent.addSubcommand(sub);
        return sub;
    }

    private void addSuggestions(final @NonNull ArgumentWord argument) {
        argument.setSuggestionCallback((sender, context, suggestion) -> {
            final C cloudSender = this.manager.senderMapper().map(sender);
            final Suggestions<C, ?> result = this.manager.suggestionFactory().suggestImmediately(cloudSender, context.getInput());

            for (final Suggestion s : result.list()) {
                final String trimmed = StringUtils.trimBeforeLastSpace(s.suggestion(), result.commandInput());

                if (trimmed != null) {
                    suggestion.addEntry(new SuggestionEntry(trimmed));
                }
            }
        });
    }
}
