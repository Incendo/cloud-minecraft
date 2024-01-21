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
package cloud.commandframework.sponge.parser;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.sponge.NodeSource;

/**
 * Parent of {@link Vector3dParser} and {@link Vector2dParser} containing shared methods.
 *
 * <p>Not for extension by API users.</p>
 *
 * @param <C> command sender type
 * @param <V> vector type
 */
public abstract class VectorParser<C, V> implements NodeSource, ArgumentParser.FutureArgumentParser<C, V>, SuggestionProvider<C> {

    private final boolean centerIntegers;

    protected VectorParser(final boolean centerIntegers) {
        this.centerIntegers = centerIntegers;
    }

    /**
     * Get whether integers will be centered to x.5. Defaults to false.
     *
     * @return whether integers will be centered
     */
    public final boolean centerIntegers() {
        return this.centerIntegers;
    }

}
