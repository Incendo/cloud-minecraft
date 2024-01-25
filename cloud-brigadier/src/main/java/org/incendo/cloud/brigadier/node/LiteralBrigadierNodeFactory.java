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
package org.incendo.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.argument.ArgumentTypeFactory;
import org.incendo.cloud.brigadier.argument.BrigadierMapping;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionPredicate;
import org.incendo.cloud.brigadier.suggestion.BrigadierSuggestionFactory;
import org.incendo.cloud.brigadier.suggestion.CloudDelegatingSuggestionProvider;
import org.incendo.cloud.brigadier.suggestion.SuggestionsType;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.MappedArgumentParser;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.suggestion.SuggestionFactory;

@SuppressWarnings({"unchecked", "rawtypes"})
@API(status = API.Status.STABLE, since = "2.0.0")
public final class LiteralBrigadierNodeFactory<C, S> implements BrigadierNodeFactory<C, S, LiteralCommandNode<S>> {

    private final CloudBrigadierManager<C, S> cloudBrigadierManager;
    private final CommandManager<C> commandManager;
    private final BrigadierSuggestionFactory<C, S> brigadierSuggestionFactory;

    /**
     * Creates a new factory that produces literal command nodes.
     *
     * @param cloudBrigadierManager the brigadier manager
     * @param commandManager        the command manager
     * @param dummyContextProvider  creates the context provided when retrieving suggestions
     * @param suggestionFactory     the suggestion factory-producing tooltip suggestions
     */
    public LiteralBrigadierNodeFactory(
            final @NonNull CloudBrigadierManager<C, S> cloudBrigadierManager,
            final @NonNull CommandManager<C> commandManager,
            final @NonNull Supplier<CommandContext<C>> dummyContextProvider,
            final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory
    ) {
        this.cloudBrigadierManager = cloudBrigadierManager;
        this.commandManager = commandManager;
        this.brigadierSuggestionFactory = new BrigadierSuggestionFactory<>(
                cloudBrigadierManager,
                commandManager,
                dummyContextProvider,
                suggestionFactory
        );
    }

    @Override
    public @NonNull LiteralCommandNode<S> createNode(
            final @NonNull String label,
            final @NonNull CommandNode<C> cloudCommand,
            final @NonNull Command<S> executor,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker
    ) {
        final LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder
                .<S>literal(label)
                .requires(this.requirement(cloudCommand, permissionChecker));

        this.updateExecutes(literalArgumentBuilder, cloudCommand, executor);

        final LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();
        for (final CommandNode<C> child : cloudCommand.children()) {
            constructedRoot.addChild(this.constructCommandNode(child, permissionChecker, executor).build());
        }
        return constructedRoot;
    }

    private @NonNull BrigadierPermissionPredicate<C, S> requirement(
            final @NonNull CommandNode<C> cloudCommand,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker
    ) {
        return new BrigadierPermissionPredicate<>(this.cloudBrigadierManager.senderMapper(), permissionChecker, cloudCommand);
    }

    @Override
    public @NonNull LiteralCommandNode<S> createNode(
            final @NonNull String label,
            final org.incendo.cloud.@NonNull Command<C> cloudCommand,
            final @NonNull Command<S> executor,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker
    ) {
        final CommandNode<C> node =
                this.commandManager.commandTree().getNamedNode(cloudCommand.rootComponent().name());
        Objects.requireNonNull(node, "node");

        return this.createNode(label, node, executor, permissionChecker);
    }

    @Override
    public @NonNull LiteralCommandNode<S> createNode(
            final @NonNull String label,
        final org.incendo.cloud.@NonNull Command<C> cloudCommand,
            final @NonNull Command<S> executor
    ) {
        return this.createNode(label, cloudCommand, executor,
                (sender, permission) -> this.commandManager.testPermission(sender, permission).allowed());
    }

    private @NonNull ArgumentBuilder<S, ?> constructCommandNode(
            final @NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker,
            final com.mojang.brigadier.@NonNull Command<S> executor
    ) {
        if (root.component().parser() instanceof AggregateParser) {
            final AggregateParser<C, ?> aggregateParser = (AggregateParser<C, ?>) root.component().parser();
            return this.constructAggregateNode(
                    aggregateParser,
                    root,
                    permissionChecker,
                    executor
            );
        }

        final ArgumentBuilder<S, ?> argumentBuilder;
        if (root.component().type() == CommandComponent.ComponentType.LITERAL) {
            argumentBuilder = this.createLiteralArgumentBuilder(root.component(), root, permissionChecker);
        } else {
            argumentBuilder = this.createVariableArgumentBuilder(root.component(), root, permissionChecker);
        }
        this.updateExecutes(argumentBuilder, root, executor);
        for (final CommandNode<C> node : root.children()) {
            argumentBuilder.then(this.constructCommandNode(node, permissionChecker, executor));
        }
        return argumentBuilder;
    }

    private @NonNull ArgumentBuilder<S, ?> createLiteralArgumentBuilder(
            final @NonNull CommandComponent<C> component,
            final @NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker
    ) {
        return LiteralArgumentBuilder.<S>literal(component.name())
                .requires(this.requirement(root, permissionChecker));
    }

    private @NonNull ArgumentBuilder<S, ?> createVariableArgumentBuilder(
            final @NonNull CommandComponent<C> component,
            final @NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker
    ) {
        final ArgumentMapping<S> argumentMapping = this.getArgument(
                component.valueType(),
                component.parser()
        );

        final SuggestionProvider<S> provider;
        if (argumentMapping.suggestionsType() == SuggestionsType.CLOUD_SUGGESTIONS) {
            provider = new CloudDelegatingSuggestionProvider<>(this.brigadierSuggestionFactory, root);
        } else {
            provider = argumentMapping.suggestionProvider();
        }

        return RequiredArgumentBuilder
                .<S, Object>argument(component.name(), (ArgumentType<Object>) argumentMapping.argumentType())
                .suggests(provider)
                .requires(this.requirement(root, permissionChecker));
    }

    private @NonNull ArgumentBuilder<S, ?> constructAggregateNode(
            final @NonNull AggregateParser<C, ?> aggregateParser,
            final @NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker,
            final com.mojang.brigadier.@NonNull Command<S> executor
    ) {
        final Iterator<CommandComponent<C>> components = aggregateParser.components().iterator();
        final List<ArgumentBuilder<S, ?>> argumentBuilders = new ArrayList<>();

        while (components.hasNext()) {
            final CommandComponent<C> component = components.next();
            final ArgumentBuilder<S, ?> fragmentBuilder = this.createVariableArgumentBuilder(component, root, permissionChecker);

            if (this.cloudBrigadierManager.settings().get(BrigadierSetting.FORCE_EXECUTABLE)) {
                fragmentBuilder.executes(executor);
            }

            argumentBuilders.add(fragmentBuilder);
        }

        // We now want to link up all subsequent components to the tail.
        final ArgumentBuilder<S, ?> tail = argumentBuilders.get(argumentBuilders.size() - 1);
        for (final CommandNode<C> node : root.children()) {
            tail.then(this.constructCommandNode(node, permissionChecker, executor));
        }

        this.updateExecutes(tail, root, executor);

        // We now have the arguments constructed in order. We now want to link them up.
        // We have to do this backwards, as we cannot modify the node after it has been added to the node before it.
        for (int i = argumentBuilders.size() - 1; i > 0; i--) {
            argumentBuilders.get(i - 1).then(argumentBuilders.get(i));
        }

        return argumentBuilders.get(0);
    }

    /**
     * Returns a mapping to a Brigadier argument for the given {@code argumentParser} that produces values of the given
     * {@code valueType}.
     *
     * @param <K>            the parser type
     * @param valueType      the types of values produced by the parser
     * @param argumentParser the parser
     * @return the argument mapping
     */
    private <K extends ArgumentParser<C, ?>> @NonNull ArgumentMapping<S> getArgument(
            final @NonNull TypeToken<?> valueType,
            final @NonNull K argumentParser
    ) {
        if (argumentParser instanceof MappedArgumentParser) {
            return this.getArgument(valueType, ((MappedArgumentParser<C, ?, ?>) argumentParser).baseParser());
        }

        final BrigadierMapping<C, K, S> mapping = this.cloudBrigadierManager.mappings().mapping(argumentParser.getClass());
        if (mapping == null || mapping.mapper() == null) {
            return this.getDefaultMapping(valueType);
        }

        final SuggestionProvider<S> suggestionProvider = mapping.makeSuggestionProvider(argumentParser);
        if (suggestionProvider == BrigadierMapping.delegateSuggestions()) {
            return ImmutableArgumentMapping.<S>builder()
                    .argumentType((ArgumentType) ((Function) mapping.mapper()).apply(argumentParser))
                    .suggestionsType(SuggestionsType.CLOUD_SUGGESTIONS)
                    .build();
        }
        return ImmutableArgumentMapping.<S>builder()
                .argumentType((ArgumentType) ((Function) mapping.mapper()).apply(argumentParser))
                .suggestionProvider(suggestionProvider)
                .build();
    }

    /**
     * Returns a mapping to a Brigadier argument type from the registered default argument type suppliers.
     * If no mapping can be found, a {@link StringArgumentType#word()} is returned.
     *
     * @param type the argument type
     * @return the argument mapping
     */
    private @NonNull ArgumentMapping<S> getDefaultMapping(final @NonNull TypeToken<?> type) {
        final ArgumentTypeFactory<?> argumentTypeSupplier = this.cloudBrigadierManager.defaultArgumentTypeFactories()
                .get(GenericTypeReflector.erase(type.getType()));
        if (argumentTypeSupplier != null) {
            final ArgumentType<?> argumentType = argumentTypeSupplier.create();
            if (argumentType != null) {
                return ImmutableArgumentMapping.<S>builder()
                        .argumentType(argumentType)
                        .build();
            }
        }
        return ImmutableArgumentMapping.<S>builder()
                .argumentType(StringArgumentType.word())
                .suggestionsType(SuggestionsType.CLOUD_SUGGESTIONS)
                .build();
    }

    /**
     * Invokes {@link ArgumentBuilder#executes(Command)} on the given {@code builder} if any of the given conditions are met:
     * <ul>
     *     <li>the node is a leaf node</li>
     *     <li>the node is optional</li>
     *     <li>the node has an associated owning command</li>
     *     <li>any of the children of the node is optional</li>
     * </ul>
     *
     * @param builder  brigadier node builder
     * @param node     cloud node
     * @param executor brigadier executor
     */
    private void updateExecutes(
            final @NonNull ArgumentBuilder<S, ?> builder,
            final @NonNull CommandNode<C> node,
            final @NonNull Command<S> executor
    ) {
        if (this.cloudBrigadierManager.settings().get(BrigadierSetting.FORCE_EXECUTABLE)
                || node.isLeaf()
                || node.component().optional()
                || node.command() != null
                || node.children().stream().map(CommandNode::component)
                .filter(Objects::nonNull).anyMatch(CommandComponent::optional)) {
            builder.executes(executor);
        }
    }
}
