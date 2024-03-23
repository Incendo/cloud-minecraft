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
package org.incendo.cloud.minecraft.extras.parser;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.argument.BrigadierMappingContributor;

@API(status = API.Status.INTERNAL)
public final class ComponentBrigadierContributor implements BrigadierMappingContributor {
    @Override
    public <C, S> void contribute(
        final CommandManager<C> manager,
        final CloudBrigadierManager<C, S> brigadierManager
    ) {
        brigadierManager.registerMapping(new TypeToken<ComponentParser<C, ?>>() {}, builder -> {
            builder.cloudSuggestions().to(argument -> {
                switch (argument.stringMode()) {
                    case QUOTED:
                        return StringArgumentType.string();
                    case GREEDY:
                    case GREEDY_FLAG_YIELDING:
                        return StringArgumentType.greedyString();
                    default:
                        return StringArgumentType.word();
                }
            });
        });
    }
}
