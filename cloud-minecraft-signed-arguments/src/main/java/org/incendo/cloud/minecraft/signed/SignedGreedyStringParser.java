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

import io.leangen.geantyref.TypeToken;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.StringParser;

/**
 * Parser for signed greedy strings.
 *
 * @param <C> command sender type
 */
@DefaultQualifier(NonNull.class)
public final class SignedGreedyStringParser<C> implements
    ArgumentParser.FutureArgumentParser<C, SignedString>,
    ParserDescriptor<C, SignedString> {

    private final ArgumentParser<C, SignedString> wrapped;

    /**
     * Creates a {@link SignedGreedyStringParser} with the specified {@link SignedStringMapper mapper}.
     *
     * @param mapper signed string mapper
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SignedGreedyStringParser(final SignedStringMapper mapper) {
        this.wrapped = StringParser.<C>greedyStringParser().parser().flatMapSuccess((BiFunction) mapper);
    }

    /**
     * Creates a {@link SignedGreedyStringParser} with the default {@link SignedStringMapper mapper}.
     */
    public SignedGreedyStringParser() {
        this(SignedStringMapper.get());
    }

    @Override
    public CompletableFuture<ArgumentParseResult<SignedString>> parseFuture(
        final CommandContext<C> commandContext,
        final CommandInput commandInput
    ) {
        return this.wrapped.parseFuture(commandContext, commandInput);
    }

    @Override
    public ArgumentParser<C, SignedString> parser() {
        return this;
    }

    @Override
    public TypeToken<SignedString> valueType() {
        return TypeToken.get(SignedString.class);
    }
}
