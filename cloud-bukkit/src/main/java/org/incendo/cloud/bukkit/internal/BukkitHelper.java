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
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.BukkitCommandMeta;
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
     * @param label label
     * @return namespaced label
     */
    public static @NonNull String namespacedLabel(final @NonNull BukkitCommandManager<?> manager, final @NonNull String label) {
        return (manager.owningPlugin().getName() + ':' + label).toLowerCase(Locale.ROOT);
    }

    /**
     * Strips the owning plugin namespace from a command.
     *
     * @param manager command manager
     * @param command command line
     * @return modified command line
     */
    public static String stripNamespace(final @NonNull BukkitCommandManager<?> manager, final @NonNull String command) {
        final String[] split = command.split(" ");
        if (!split[0].contains(":")) {
            return command;
        }
        final String token = split[0];
        final String[] splitToken = token.split(":");
        if (BukkitHelper.namespacedLabel(manager, splitToken[1]).equals(token)) {
            split[0] = splitToken[1];
            return String.join(" ", split);
        }
        return command;
    }
}
