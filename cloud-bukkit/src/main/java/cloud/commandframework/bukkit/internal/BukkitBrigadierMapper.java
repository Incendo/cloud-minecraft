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
package cloud.commandframework.bukkit.internal;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.UUIDParser;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.parser.BlockPredicateParser;
import cloud.commandframework.bukkit.parser.EnchantmentParser;
import cloud.commandframework.bukkit.parser.ItemStackParser;
import cloud.commandframework.bukkit.parser.ItemStackPredicateParser;
import cloud.commandframework.bukkit.parser.NamespacedKeyParser;
import cloud.commandframework.bukkit.parser.location.Location2DParser;
import cloud.commandframework.bukkit.parser.location.LocationParser;
import cloud.commandframework.bukkit.parser.selector.MultipleEntitySelectorParser;
import cloud.commandframework.bukkit.parser.selector.MultiplePlayerSelectorParser;
import cloud.commandframework.bukkit.parser.selector.SingleEntitySelectorParser;
import cloud.commandframework.bukkit.parser.selector.SinglePlayerSelectorParser;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Helper for mapping argument types to their NMS Brigadier counterpart on CraftBukkit platforms.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.INTERNAL)
public final class BukkitBrigadierMapper<C> {

    private final BukkitCommandManager<C> commandManager;
    private final CloudBrigadierManager<C, ?> brigadierManager;

    /**
     * Class that handles mapping argument types to Brigadier for Bukkit (Commodore) and Paper.
     *
     * @param commandManager   The {@link BukkitCommandManager} to use for mapping
     * @param brigadierManager The {@link CloudBrigadierManager} to use for mapping
     */
    public BukkitBrigadierMapper(
        final @NonNull BukkitCommandManager<C> commandManager,
        final @NonNull CloudBrigadierManager<C, ?> brigadierManager
    ) {
        this.commandManager = commandManager;
        this.brigadierManager = brigadierManager;
    }

    /**
     * Register Brigadier mappings for cloud-bukkit parsers.
     */
    @SuppressWarnings("unused")
    public void registerBuiltInMappings() {
        /* UUID nms argument is a 1.16+ feature */
        try {
            final Class<? extends ArgumentType<?>> uuid = MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("uuid"));
            /* Map UUID */
            this.mapSimpleNMS(new TypeToken<UUIDParser<C>>() {}, "uuid");
        } catch (final IllegalArgumentException ignore) {
            // < 1.16
        }
        /* Map NamespacedKey */
        this.mapSimpleNMS(new TypeToken<NamespacedKeyParser<C>>() {}, "resource_location", true);
        /* Map Enchantment */
        try {
            // Pre-1.19.3
            final Class<? extends ArgumentType<?>> ench = MinecraftArgumentTypes.getClassByKey(
                NamespacedKey.minecraft("item_enchantment"));
            this.mapSimpleNMS(new TypeToken<EnchantmentParser<C>>() {}, "item_enchantment");
        } catch (final IllegalArgumentException ignore) {
            // 1.19.3+
            this.mapResourceKey(new TypeToken<EnchantmentParser<C>>() {}, "enchantment");
        }
        /* Map Item arguments */
        this.mapSimpleNMS(new TypeToken<ItemStackParser<C>>() {}, "item_stack");
        this.mapSimpleNMS(new TypeToken<ItemStackPredicateParser<C>>() {}, "item_predicate");
        /* Map Block arguments */
        this.mapSimpleNMS(new TypeToken<BlockPredicateParser<C>>() {}, "block_predicate");
        /* Map Entity Selectors */
        this.mapSelector(new TypeToken<SingleEntitySelectorParser<C>>() {}, true, false);
        this.mapSelector(new TypeToken<SinglePlayerSelectorParser<C>>() {}, true, true);
        this.mapSelector(new TypeToken<MultipleEntitySelectorParser<C>>() {}, false, false);
        this.mapSelector(new TypeToken<MultiplePlayerSelectorParser<C>>() {}, false, true);
        /* Map Vec3 */
        this.mapNMS(new TypeToken<LocationParser<C>>() {}, "vec2", this::argumentVec3);
        /* Map Vec2 */
        this.mapNMS(new TypeToken<Location2DParser<C>>() {}, "vec3", this::argumentVec2);
    }

    private <T extends ArgumentParser<C, ?>> void mapResourceKey(
        final @NonNull TypeToken<T> parserType,
        final @NonNull String registryName
    ) {
        this.mapNMS(parserType, "resource_key", type -> (ArgumentType<?>) type.getDeclaredConstructors()[0]
            .newInstance(RegistryReflection.registryKey(RegistryReflection.registryByName(registryName))));
    }

    /**
     * @param <T>         parser type
     * @param parserType  parser type
     * @param single      Whether the selector is for a single entity only (true), or for multiple entities (false)
     * @param playersOnly Whether the selector is for players only (true), or for all entities (false)
     */
    private <T extends ArgumentParser<C, ?>> void mapSelector(
        final @NonNull TypeToken<T> parserType,
        final boolean single,
        final boolean playersOnly
    ) {
        this.mapNMS(parserType, "entity", argumentTypeCls -> {
            final Constructor<?> constructor = argumentTypeCls.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return (ArgumentType<?>) constructor.newInstance(single, playersOnly);
        });
    }

    private @NonNull ArgumentType<?> argumentVec3(final Class<? extends ArgumentType<?>> type) throws ReflectiveOperationException {
        return type.getDeclaredConstructor(boolean.class).newInstance(true);
    }

    private @NonNull ArgumentType<?> argumentVec2(final Class<? extends ArgumentType<?>> type) throws ReflectiveOperationException {
        return type.getDeclaredConstructor(boolean.class).newInstance(true);
    }

    /**
     * {@link #mapSimpleNMS(TypeToken, String, boolean)} defaulting {@code cloudSuggestions} to {@code false}
     *
     * @param type       parser type
     * @param argumentId argument type id
     * @param <T>        parser type
     */
    public <T extends ArgumentParser<C, ?>> void mapSimpleNMS(final @NonNull TypeToken<T> type, final @NonNull String argumentId) {
        this.mapSimpleNMS(type, argumentId, false);
    }

    /**
     * Register a mapping for a Minecraft argument type meeting the following criteria:
     * <ul>
     *     <li>On versions with CommandBuildContext, has a constructor with no arguments or with a CommandBuildContext
     *     as the only argument</li>
     *     <li>On versions before CommandBuildContext, has a no-args constructor.</li>
     * </ul>
     *
     * @param <T>                 argument parser type
     * @param type                Type to map
     * @param argumentId          registry id of argument type
     * @param useCloudSuggestions whether to use cloud suggestions
     */
    public <T extends ArgumentParser<C, ?>> void mapSimpleNMS(
        final @NonNull TypeToken<T> type,
        final @NonNull String argumentId,
        final boolean useCloudSuggestions
    ) {
        this.mapNMS(type, argumentId, cls -> {
            final Constructor<?> ctr = cls.getDeclaredConstructors()[0];
            final Object[] args = ctr.getParameterCount() == 1
                ? new Object[]{CommandBuildContextSupplier.commandBuildContext()}
                : new Object[]{};
            return (ArgumentType<?>) ctr.newInstance(args);
        }, useCloudSuggestions);
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type with native
     * suggestions.
     *
     * @param type       Type to map
     * @param argumentId argument type id
     * @param factory    Supplier of the NMS argument type
     * @param <T>        argument parser type
     */
    public <T extends ArgumentParser<C, ?>> void mapNMS(
        final @NonNull TypeToken<T> type,
        final @NonNull String argumentId,
        final @NonNull ArgumentTypeFactory factory
    ) {
        this.mapNMS(type, argumentId, factory, false);
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type.
     *
     * @param type             Type to map
     * @param argumentId       argument type id
     * @param factory          Supplier of the NMS argument type
     * @param cloudSuggestions whether to use cloud suggestions
     * @param <T>              argument parser type
     */
    public <T extends ArgumentParser<C, ?>> void mapNMS(
        final @NonNull TypeToken<T> type,
        final @NonNull String argumentId,
        final @NonNull ArgumentTypeFactory factory,
        final boolean cloudSuggestions
    ) {
        final Class<? extends ArgumentType<?>> argumentTypeClass =
            MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft(argumentId));
        this.brigadierManager.registerMapping(type, builder -> {
            builder.to(argument -> {
                try {
                    return factory.makeInstance(argumentTypeClass);
                } catch (final Exception e) {
                    this.commandManager.owningPlugin().getLogger().log(
                        Level.WARNING,
                        "Failed to create instance of " + argumentId + ", falling back to StringArgumentType.word()",
                        e
                    );
                    return StringArgumentType.word();
                }
            });
            if (cloudSuggestions) {
                builder.cloudSuggestions();
            }
        });
    }

    @API(status = API.Status.INTERNAL)
    @FunctionalInterface
    public interface ArgumentTypeFactory {

        /**
         * Make an instance of {@code class}.
         *
         * @param argumentTypeClass class
         * @return instance
         * @throws ReflectiveOperationException on reflection error
         */
        ArgumentType<?> makeInstance(Class<? extends ArgumentType<?>> argumentTypeClass) throws ReflectiveOperationException;
    }
}
