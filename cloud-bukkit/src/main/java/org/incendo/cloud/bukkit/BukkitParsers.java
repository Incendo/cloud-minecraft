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
package org.incendo.cloud.bukkit;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.annotation.specifier.AllowEmptySelection;
import org.incendo.cloud.bukkit.annotation.specifier.DefaultNamespace;
import org.incendo.cloud.bukkit.annotation.specifier.RequireExplicitNamespace;
import org.incendo.cloud.bukkit.data.MultipleEntitySelector;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.bukkit.parser.BlockPredicateParser;
import org.incendo.cloud.bukkit.parser.EnchantmentParser;
import org.incendo.cloud.bukkit.parser.ItemStackParser;
import org.incendo.cloud.bukkit.parser.ItemStackPredicateParser;
import org.incendo.cloud.bukkit.parser.MaterialParser;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.bukkit.parser.location.Location2DParser;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import org.incendo.cloud.parser.ParserParameters;

@API(status = API.Status.INTERNAL)
public final class BukkitParsers {
    private BukkitParsers() {
    }

    /**
     * Register the Bukkit parsers.
     *
     * @param manager manager
     * @param <C>     sender type
     */
    public static <C> void register(final CommandManager<C> manager) {
        /* Register Bukkit Parsers */
        manager.parserRegistry()
            .registerParser(WorldParser.worldParser())
            .registerParser(MaterialParser.materialParser())
            .registerParser(PlayerParser.playerParser())
            .registerParser(OfflinePlayerParser.offlinePlayerParser())
            .registerParser(EnchantmentParser.enchantmentParser())
            .registerParser(LocationParser.locationParser())
            .registerParser(Location2DParser.location2DParser())
            .registerParser(ItemStackParser.itemStackParser())
            .registerParser(SingleEntitySelectorParser.singleEntitySelectorParser())
            .registerParser(SinglePlayerSelectorParser.singlePlayerSelectorParser());

        /* Register Entity Selector Parsers */
        manager.parserRegistry().registerAnnotationMapper(
            AllowEmptySelection.class,
            (annotation, type) -> ParserParameters.single(
                BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT,
                annotation.value()
            )
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(MultipleEntitySelector.class),
            parserParameters -> new MultipleEntitySelectorParser<>(
                parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true)
            )
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(MultiplePlayerSelector.class),
            parserParameters -> new MultiplePlayerSelectorParser<>(
                parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true)
            )
        );

        if (CraftBukkitReflection.classExists("org.bukkit.NamespacedKey")) {
            registerParserSupplierFor(manager, NamespacedKeyParser.class);
            manager.parserRegistry().registerAnnotationMapper(
                RequireExplicitNamespace.class,
                (annotation, type) -> ParserParameters.single(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE, true)
            );
            manager.parserRegistry().registerAnnotationMapper(
                DefaultNamespace.class,
                (annotation, type) -> ParserParameters.single(BukkitParserParameters.DEFAULT_NAMESPACE, annotation.value())
            );
        }

        /* Register MC 1.13+ parsers */
        if (manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            registerParserSupplierFor(manager, ItemStackPredicateParser.class);
            registerParserSupplierFor(manager, BlockPredicateParser.class);
        }
    }

    /**
     * Attempts to call the method on the provided class matching the signature
     * <p>{@code private static void registerParserSupplier(CommandManager)}</p>
     * using reflection.
     *
     * @param manager       manager
     * @param argumentClass argument class
     */
    private static void registerParserSupplierFor(final CommandManager<?> manager, final @NonNull Class<?> argumentClass) {
        try {
            final Method registerParserSuppliers = argumentClass
                .getDeclaredMethod("registerParserSupplier", CommandManager.class);
            registerParserSuppliers.setAccessible(true);
            registerParserSuppliers.invoke(null, manager);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
