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
package org.incendo.cloud.minecraft.signed;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;

@API(status = API.Status.INTERNAL)
public final class SignedArguments {
    private SignedArguments() {
    }

    /**
     * Returns whether Adventure is present.
     *
     * @return whether Adventure is present
     */
    public static boolean adventurePresent() {
        try {
            Class.forName("net.kyori.adventure.chat.SignedMessage");
            return true;
        } catch (final ClassNotFoundException ignore) {
            return false;
        }
    }

    /**
     * Registers the default Brigadier mapping for {@link SignedGreedyStringParser} if the manager
     * is a {@link BrigadierManagerHolder}.
     *
     * @param brigadierManager command manager
     * @param <C>              command sender type
     */
    @SuppressWarnings("unchecked")
    public static <C> void registerDefaultBrigadierMapping(final Object brigadierManager) {
        ((CloudBrigadierManager<C, ?>) brigadierManager).registerMapping(
            new TypeToken<SignedGreedyStringParser<C>>() {},
            builder -> builder.toConstant(StringArgumentType.greedyString()).cloudSuggestions()
        );
    }
}
