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
package org.incendo.cloud.paper;

import io.papermc.paper.plugin.configuration.PluginMeta;
import java.util.Objects;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.bukkit.PluginHolder;

/**
 * Interface implemented by managers that have an associated PluginMeta.
 */
@SuppressWarnings("UnstableApiUsage")
public interface PluginMetaHolder extends PluginHolder {
    /**
     * Returns the meta of the plugin that owns the manager.
     *
     * @return owning plugin meta
     */
    PluginMeta owningPluginMeta();

    /**
     * Returns the plugin instance for {@link #owningPluginMeta()}.
     *
     * @return the plugin instance
     * @throws NullPointerException when the plugin is not loaded yet
     */
    @Override
    default Plugin owningPlugin() {
        return Objects.requireNonNull(
            Bukkit.getPluginManager().getPlugin(this.owningPluginMeta().getName()),
            () -> this.owningPluginMeta().getName() + " Plugin instance"
        );
    }

    /**
     * Creates a meta holder from a plugin holder.
     *
     * @param pluginHolder plugin holder
     * @return meta holder
     */
    @API(status = API.Status.INTERNAL)
    static PluginMetaHolder fromPluginHolder(final PluginHolder pluginHolder) {
        return new PluginMetaHolder() {
            @Override
            public PluginMeta owningPluginMeta() {
                return pluginHolder.owningPlugin().getPluginMeta();
            }

            @Override
            public Plugin owningPlugin() {
                return pluginHolder.owningPlugin();
            }
        };
    }
}
