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
package org.incendo.cloud.waterdog;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;

/**
 * Command preprocessor which decorates incoming {@link org.incendo.cloud.context.CommandContext}
 * with WaterdogPE specific objects
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
final class WaterdogCommandPreprocessor<C> implements CommandPreprocessor<C> {

    private final WaterdogCommandManager<C> mgr;

    /**
     * The WaterdogPE Command Preprocessor for storing WaterdogPE-specific contexts in the command contexts
     *
     * @param mgr The WaterdogCommandManager
     */
    WaterdogCommandPreprocessor(final @NonNull WaterdogCommandManager<C> mgr) {
        this.mgr = mgr;
    }

    @Override
    public void accept(final @NonNull CommandPreprocessingContext<C> context) {
        context.commandContext().store(WaterdogContextKeys.PROXY_SERVER_KEY, this.mgr.owningPlugin().getProxy());
    }
}
