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

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.kyori.adventure.util.Services;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserRegistry;

import static org.incendo.cloud.minecraft.signed.SignedGreedyStringParser.signedGreedyStringParser;

@DefaultQualifier(NonNull.class)
public interface SignedStringMapper extends BiFunction<CommandContext<?>,
    String, CompletableFuture<ArgumentParseResult<SignedString>>> {

    /**
     * Returns the default {@link SignedStringMapper} as loaded by {@link java.util.ServiceLoader}.
     *
     * @return the signed mapper
     */
    static SignedStringMapper get() {
        return Services.serviceWithFallback(SignedStringMapper.class)
            .orElseThrow(() -> new IllegalStateException("Could not locate " + SignedStringMapper.class.getName()));
    }

    /**
     * Registers {@link SignedGreedyStringParser} to the parser registry, and to the
     * {@link org.incendo.cloud.brigadier.CloudBrigadierManager} if applicable.
     *
     * @param manager command manager
     */
    default void registerParser(final CommandManager<?> manager) {
        this.registerParser(manager.parserRegistry());
        if (SignedArguments.brigadierPresent()) {
            this.registerBrigadier(manager);
        }
    }

    /**
     * Registers the {@link SignedGreedyStringParser} to the {@link org.incendo.cloud.brigadier.CloudBrigadierManager}.
     *
     * <p>Implementations must check if the manager is a {@link org.incendo.cloud.brigadier.BrigadierManagerHolder}.</p>
     *
     * @param manager command manager
     */
    void registerBrigadier(CommandManager<?> manager);

    /**
     * Registers {@link SignedGreedyStringParser} to the parser registry.
     *
     * @param registry parser registry
     */
    default void registerParser(final ParserRegistry<?> registry) {
        registry.registerParser(signedGreedyStringParser());
    }

    final class Unsigned implements SignedStringMapper, Services.Fallback {

        @Override
        public void registerBrigadier(final CommandManager<?> manager) {
            SignedArguments.registerDefaultBrigadierMapping(manager);
        }

        @Override
        public CompletableFuture<ArgumentParseResult<SignedString>> apply(
            final CommandContext<?> commanderCommandContext,
            final String s
        ) {
            return ArgumentParseResult.successFuture(SignedString.unsigned(s));
        }
    }
}
