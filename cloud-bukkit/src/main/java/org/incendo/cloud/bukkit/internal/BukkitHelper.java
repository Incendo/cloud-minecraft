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
package org.incendo.cloud.bukkit.internal;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.BukkitCommandMeta;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.description.CommandDescription;

@API(status = API.Status.INTERNAL)
public final class BukkitHelper {
    private BukkitHelper() {
    }

    /**
     * Get the Bukkit description for a cloud command.
     *
     * @param command command
     * @return bukkit description
     */
    public static @NonNull String description(final @NonNull Command<?> command) {
        final Optional<String> bukkitDescription = command.commandMeta().optional(BukkitCommandMeta.BUKKIT_DESCRIPTION);
        if (bukkitDescription.isPresent()) {
            return bukkitDescription.get();
        }

        final CommandDescription description = command.commandDescription();
        if (!description.isEmpty()) {
            return description.description().textDescription();
        }

        return command.rootComponent().description().textDescription();
    }

    /**
     * Returns the namespaced version of a label.
     *
     * @param manager manager
     * @param label   label
     * @return namespaced label
     */
    public static @NonNull String namespacedLabel(final @NonNull PluginHolder manager, final @NonNull String label) {
        return namespacedLabel(manager.owningPlugin().getName(), label);
    }

    /**
     * Returns the namespaced version of a label.
     *
     * @param pluginName plugin name
     * @param label      label
     * @return namespaced label
     */
    public static @NonNull String namespacedLabel(final @NonNull String pluginName, final @NonNull String label) {
        return (pluginName + ':' + label).toLowerCase(Locale.ROOT);
    }

    /**
     * Strips the owning plugin namespace from a command.
     *
     * @param manager manager
     * @param command command line
     * @return modified command line
     */
    public static @NonNull String stripNamespace(final @NonNull PluginHolder manager, final @NonNull String command) {
        return stripNamespace(manager.owningPlugin().getName(), command);
    }

    /**
     * Strips the owning plugin namespace from a command.
     *
     * @param pluginName plugin name
     * @param command    command line
     * @return modified command line
     */
    public static @NonNull String stripNamespace(final @NonNull String pluginName, final @NonNull String command) {
        final String[] split = command.split(" ");
        if (!split[0].contains(":")) {
            return command;
        }
        final String token = split[0];
        final String[] splitToken = token.split(":");
        if (namespacedLabel(pluginName, splitToken[1]).equals(token)) {
            split[0] = splitToken[1];
            return String.join(" ", split);
        }
        return command;
    }

    /**
     * Bukkit main thread executor.
     *
     * @param pluginHolder plugin holder
     * @return executor
     */
    public static @NonNull Executor mainThreadExecutor(final @NonNull PluginHolder pluginHolder) {
        final Plugin plugin = pluginHolder.owningPlugin();
        final Server server = plugin.getServer();
        return task -> {
            if (server.isPrimaryThread()) {
                task.run();
                return;
            }
            server.getScheduler().runTask(plugin, task);
        };
    }
}
