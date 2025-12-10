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
package org.incendo.cloud.bukkit.parser;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.data.BlockPredicate;
import org.incendo.cloud.bukkit.internal.CommandBuildContextSupplier;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.bukkit.internal.MinecraftArgumentTypes;
import org.incendo.cloud.bukkit.internal.RegistryReflection;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;

/**
 * Parser for {@link BlockPredicate}.
 *
 * <p>This argument type is only usable on Minecraft 1.13+, as it depends on Minecraft internals added in that version.</p>
 *
 * <p>This argument type only provides basic suggestions by default. Enabling Brigadier compatibility through
 * {@link BukkitCommandManager#registerBrigadier()} will allow client side validation and suggestions to be utilized.</p>
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public final class BlockPredicateParser<C> implements ArgumentParser.FutureArgumentParser<C, BlockPredicate> {

    /**
     * Creates a block predicate parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, BlockPredicate> blockPredicateParser() {
        return ParserDescriptor.of(new BlockPredicateParser<>(), BlockPredicate.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #blockPredicateParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, BlockPredicate> blockPredicateComponent() {
        return CommandComponent.<C, BlockPredicate>builder().parser(blockPredicateParser());
    }

    private final ArgumentParser<C, BlockPredicate> parser;

    /**
     * Create a new {@link BlockPredicateParser}.
     *
     * @since 1.5.0
     */
    public BlockPredicateParser() {
        this.parser = this.createParser();
    }

    @SuppressWarnings("unchecked")
    private ArgumentParser<C, BlockPredicate> createParser() {
        final Supplier<ArgumentType<Object>> inst = () -> {
            final Constructor<?> ctr = Reflection.ARGUMENT_BLOCK_PREDICATE_CLASS.get().getDeclaredConstructors()[0];
            try {
                if (ctr.getParameterCount() == 0) {
                    return (ArgumentType<Object>) ctr.newInstance();
                } else {
                    // 1.19+
                    return (ArgumentType<Object>) ctr.newInstance(CommandBuildContextSupplier.commandBuildContext());
                }
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize BlockPredicate parser.", e);
            }
        };
        return new WrappedBrigadierParser<C, Object>(inst).flatMapSuccess((ctx, result) -> {
            if (result instanceof Predicate) {
                // 1.19+
                return ArgumentParseResult.successFuture(new BlockPredicateImpl((Predicate<Object>) result));
            }
            final Object commandSourceStack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
            try {
                final Object server = Reflection.GET_SERVER_METHOD.invoke(commandSourceStack);
                final Object obj;
                if (Reflection.GET_TAG_REGISTRY_METHOD != null) {
                    obj = Reflection.GET_TAG_REGISTRY_METHOD.invoke(server);
                } else {
                    obj = RegistryReflection.builtInRegistryByName("block");
                }
                Objects.requireNonNull(Reflection.CREATE_PREDICATE_METHOD, "create on BlockPredicateArgument$Result");
                final Predicate<Object> predicate = (Predicate<Object>) Reflection.CREATE_PREDICATE_METHOD.invoke(result, obj);
                return ArgumentParseResult.successFuture(new BlockPredicateImpl(predicate));
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull BlockPredicate>> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return this.parser.parseFuture(commandContext, commandInput);
    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.parser.suggestionProvider();
    }

    /**
     * Called reflectively by {@link org.incendo.cloud.bukkit.BukkitParsers}.
     *
     * @param commandManager command manager
     * @param <C>            sender type
     */
    @SuppressWarnings("unused")
    private static <C> void registerParserSupplier(final @NonNull CommandManager<C> commandManager) {
        commandManager.parserRegistry().registerParser(BlockPredicateParser.blockPredicateParser());
    }


    private static final class BlockPredicateImpl implements BlockPredicate {

        private final Predicate<Object> predicate;

        BlockPredicateImpl(final @NonNull Predicate<Object> predicate) {
            this.predicate = predicate;
        }

        private boolean testImpl(final @NonNull Block block, final boolean loadChunks) {
            try {
                final Object blockInWorld = Reflection.SHAPE_DETECTOR_BLOCK_CTR.newInstance(
                        Reflection.GET_HANDLE_METHOD.invoke(block.getWorld()),
                        Reflection.BLOCK_POSITION_CTR.newInstance(block.getX(), block.getY(), block.getZ()),
                        loadChunks
                );
                return this.predicate.test(blockInWorld);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean test(final @NonNull Block block) {
            return this.testImpl(block, false);
        }

        @Override
        public @NonNull BlockPredicate loadChunks() {
            return new BlockPredicate() {
                @Override
                public @NonNull BlockPredicate loadChunks() {
                    return this;
                }

                @Override
                public boolean test(final Block block) {
                    return BlockPredicateImpl.this.testImpl(block, true);
                }
            };
        }
    }

    private static final class Reflection {
        private static final Class<?> TAG_CONTAINER_CLASS;

        static {
            Class<?> tagContainerClass;
            if (CraftBukkitReflection.MAJOR_REVISION > 12 && CraftBukkitReflection.MAJOR_REVISION < 16) {
                tagContainerClass = CraftBukkitReflection.needNMSClass("TagRegistry");
            } else {
                tagContainerClass = CraftBukkitReflection.firstNonNullOrThrow(
                    () -> "tagContainerClass",
                    CraftBukkitReflection.findNMSClass("ITagRegistry"),
                    CraftBukkitReflection.findMCClass("tags.ITagRegistry"),
                    CraftBukkitReflection.findMCClass("tags.TagContainer"),
                    CraftBukkitReflection.findMCClass("core.IRegistry"),
                    CraftBukkitReflection.findMCClass("core.Registry")
                );
            }
            TAG_CONTAINER_CLASS = tagContainerClass;
        }

        private static final Class<?> CRAFT_WORLD_CLASS = CraftBukkitReflection.needOBCClass("CraftWorld");
        private static final Class<?> MINECRAFT_SERVER_CLASS = CraftBukkitReflection.needNMSClassOrElse(
            "MinecraftServer",
            "net.minecraft.server.MinecraftServer"
        );
        private static final Class<?> COMMAND_LISTENER_WRAPPER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Couldn't find CommandSourceStack class",
            CraftBukkitReflection.findNMSClass("CommandListenerWrapper"),
            CraftBukkitReflection.findMCClass("commands.CommandListenerWrapper"),
            CraftBukkitReflection.findMCClass("commands.CommandSourceStack")
        );
        private static final Supplier<Class<?>> ARGUMENT_BLOCK_PREDICATE_CLASS =
            Suppliers.memoize(() -> MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("block_predicate")));
        private static final Class<?> ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Couldn't find BlockPredicateArgument$Result class",
            CraftBukkitReflection.findNMSClass("ArgumentBlockPredicate$b"),
            CraftBukkitReflection.findMCClass("commands.arguments.blocks.ArgumentBlockPredicate$b"),
            CraftBukkitReflection.findMCClass("commands.arguments.blocks.BlockPredicateArgument$Result")
        );
        private static final Class<?> SHAPE_DETECTOR_BLOCK_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Couldn't find BlockInWorld class",
            CraftBukkitReflection.findNMSClass("ShapeDetectorBlock"),
            CraftBukkitReflection.findMCClass("world.level.block.state.pattern.ShapeDetectorBlock"),
            CraftBukkitReflection.findMCClass("world.level.block.state.pattern.BlockInWorld")
        );
        private static final Class<?> LEVEL_READER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Couldn't find LevelReader class",
            CraftBukkitReflection.findNMSClass("IWorldReader"),
            CraftBukkitReflection.findMCClass("world.level.IWorldReader"),
            CraftBukkitReflection.findMCClass("world.level.LevelReader")
        );
        private static final Class<?> BLOCK_POSITION_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Couldn't find BlockPos class",
            CraftBukkitReflection.findNMSClass("BlockPosition"),
            CraftBukkitReflection.findMCClass("core.BlockPosition"),
            CraftBukkitReflection.findMCClass("core.BlockPos")
        );
        private static final Constructor<?> BLOCK_POSITION_CTR =
            CraftBukkitReflection.needConstructor(BLOCK_POSITION_CLASS, int.class, int.class, int.class);
        private static final Constructor<?> SHAPE_DETECTOR_BLOCK_CTR = CraftBukkitReflection
            .needConstructor(SHAPE_DETECTOR_BLOCK_CLASS, LEVEL_READER_CLASS, BLOCK_POSITION_CLASS, boolean.class);
        private static final Method GET_HANDLE_METHOD = CraftBukkitReflection.needMethod(CRAFT_WORLD_CLASS, "getHandle");
        private static final @Nullable Method CREATE_PREDICATE_METHOD = CraftBukkitReflection.firstNonNullOrNull(
            CraftBukkitReflection.findMethod(ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS, "create", TAG_CONTAINER_CLASS),
            CraftBukkitReflection.findMethod(ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS, "a", TAG_CONTAINER_CLASS)
        );
        private static final Method GET_SERVER_METHOD = CraftBukkitReflection.streamMethods(COMMAND_LISTENER_WRAPPER_CLASS)
            .filter(it -> it.getReturnType().equals(MINECRAFT_SERVER_CLASS) && it.getParameterCount() == 0)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find CommandSourceStack#getServer."));
        private static final @Nullable Method GET_TAG_REGISTRY_METHOD = CraftBukkitReflection.firstNonNullOrNull(
            CraftBukkitReflection.findMethod(MINECRAFT_SERVER_CLASS, "getTagRegistry"),
            CraftBukkitReflection.findMethod(MINECRAFT_SERVER_CLASS, "getTags"),
            CraftBukkitReflection.streamMethods(MINECRAFT_SERVER_CLASS)
                .filter(it -> it.getReturnType().equals(TAG_CONTAINER_CLASS) && it.getParameterCount() == 0)
                .findFirst()
                .orElse(null)
        );
    }
}
