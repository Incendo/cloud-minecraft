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
package org.incendo.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.mojang.brigadier.Message;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;

final class ReflectiveCompletionMapper implements CompletionMapper {

    private final CompletionMapper wrapped;

    ReflectiveCompletionMapper() {
        if (CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.MessageComponentSerializer")) {
            this.wrapped = new Modern();
        } else {
            this.wrapped = new Legacy();
        }
    }

    @Override
    public AsyncTabCompleteEvent.@NonNull Completion map(final @NonNull TooltipSuggestion suggestion) {
        return this.wrapped.map(suggestion);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static final class Modern implements CompletionMapper {
        private final Object serializer;
        private final Method deserializeOrNull;
        private final Method completionWithTooltipMethod;

        Modern() {
            final Method instance = CraftBukkitReflection.needMethod(
                MessageComponentSerializer.class,
                "message"
            );
            try {
                this.serializer = instance.invoke(null);
                this.deserializeOrNull = CraftBukkitReflection.needMethod(
                    MessageComponentSerializer.class,
                    "deserializeOrNull",
                    Message.class
                );
                this.completionWithTooltipMethod = CraftBukkitReflection.needMethod(
                    AsyncTabCompleteEvent.Completion.class,
                    "completion",
                    String.class,
                    this.deserializeOrNull.getReturnType()
                );
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public AsyncTabCompleteEvent.@NonNull Completion map(final @NonNull TooltipSuggestion suggestion) {
            try {
                return (AsyncTabCompleteEvent.Completion) this.completionWithTooltipMethod.invoke(
                    suggestion.suggestion(),
                    this.deserializeOrNull.invoke(this.serializer, suggestion.tooltip())
                );
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("removal")
    private static final class Legacy implements CompletionMapper {
        private final MethodHandle completionWithTooltip;
        private final MethodHandle componentFromMessage;

        Legacy() {
            final Method componentFromMessageMethod = CraftBukkitReflection.needMethod(
                io.papermc.paper.brigadier.PaperBrigadier.class,
                "componentFromMessage",
                Message.class
            );
            final Method completionWithTooltipMethod = CraftBukkitReflection.needMethod(
                AsyncTabCompleteEvent.Completion.class,
                "completion",
                String.class,
                componentFromMessageMethod.getReturnType()
            );
            try {
                this.componentFromMessage = MethodHandles.publicLookup().unreflect(componentFromMessageMethod);
                this.completionWithTooltip = MethodHandles.publicLookup().unreflect(completionWithTooltipMethod);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public AsyncTabCompleteEvent.@NonNull Completion map(final @NonNull TooltipSuggestion suggestion) {
            final Message tooltip = suggestion.tooltip();
            if (tooltip == null) {
                return AsyncTabCompleteEvent.Completion.completion(suggestion.suggestion());
            }
            try {
                final Object component = this.componentFromMessage.invoke(tooltip);
                return (AsyncTabCompleteEvent.Completion) this.completionWithTooltip.invoke(suggestion.suggestion(), component);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
