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
package org.incendo.cloud.minestom.parser.location;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.DoubleParser;

/**
 * Parser for Vec/Pos components.
 *
 * @since 2.0.0
 * @param <C> command sender type
 */
final class LocationCoordinate<C> implements ArgumentParser<C, LocationCoordinate<C>> {

    private final LocationCoordinateType type;
    private final double value;

    private LocationCoordinate(final @NonNull LocationCoordinateType type, final double value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Creates a new coordinate component.
     *
     * @param <C>   command sender type
     * @param type  the coordinate type
     * @param value the numeric offset
     * @return the created component
     */
    static <C> @NonNull LocationCoordinate<C> of(final @NonNull LocationCoordinateType type, final double value) {
        return new LocationCoordinate<>(type, value);
    }

    @Override
    public @NonNull ArgumentParseResult<LocationCoordinate<C>> parse(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.skipWhitespace().peekString();

        final LocationCoordinateType coordinateType;
        if (commandInput.peek() == '~') {
            coordinateType = LocationCoordinateType.RELATIVE;
            commandInput.moveCursor(1);
        } else {
            coordinateType = LocationCoordinateType.ABSOLUTE;
        }

        final double coordinate;
        try {
            final boolean empty = commandInput.peekString().isEmpty() || commandInput.peek() == ' ';
            coordinate = empty ? 0 : commandInput.readDouble();
            if (commandInput.hasRemainingInput()) {
                commandInput.skipWhitespace();
            }
        } catch (final Exception e) {
            return ArgumentParseResult.failure(new DoubleParser.DoubleParseException(
                input,
                new DoubleParser<>(DoubleParser.DEFAULT_MINIMUM, DoubleParser.DEFAULT_MAXIMUM),
                commandContext
            ));
        }

        return ArgumentParseResult.success(LocationCoordinate.of(coordinateType, coordinate));
    }

    /**
     * Returns the final coordinate value against an origin.
     *
     * @param origin the origin value (x/y/z of sender etc)
     * @return the final coordinate
     */
    double resolve(final double origin) {
        return this.type == LocationCoordinateType.RELATIVE ? origin + this.value : this.value;
    }

    /**
     * Returns the coordinate type.
     *
     * @return coordinate type
     */
    @NonNull LocationCoordinateType type() {
        return this.type;
    }

    /**
     * Returns the absolute/relative value of this location
     *
     * @return value
     */
    double value() {
        return this.value;
    }
}
