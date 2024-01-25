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
package cloud.commandframework.bungee;

import io.leangen.geantyref.TypeToken;
import net.md_5.bungee.api.ProxyServer;
import org.incendo.cloud.key.CloudKey;

/**
 * BungeeCord related {@link org.incendo.cloud.context.CommandContext} keys
 *
 * @since 1.4.0
 */
public final class BungeeContextKeys {

    /**
     * The {@link ProxyServer} instance is stored in the {@link org.incendo.cloud.context.CommandContext}
     * in {@link BungeeCommandPreprocessor}
     */
    public static final CloudKey<ProxyServer> PROXY_SERVER_KEY = CloudKey.of(
            "ProxyServer",
            TypeToken.get(ProxyServer.class)
    );

    private BungeeContextKeys() {
    }
}
