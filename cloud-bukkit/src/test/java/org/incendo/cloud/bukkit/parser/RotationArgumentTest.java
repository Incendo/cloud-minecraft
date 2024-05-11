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
package org.incendo.cloud.bukkit.parser;

import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.parser.rotation.Rotation;
import org.incendo.cloud.bukkit.parser.rotation.RotationParser;
import org.incendo.cloud.bukkit.util.ServerTest;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RotationArgumentTest extends ServerTest {

    @ParameterizedTest
    @MethodSource("Parse_HappyFlow_Success_Source")
    void Parse_HappyFlow_Success(final @NonNull String input, final @NonNull Location expectedLocation) {
        // Arrange
        final RotationParser<CommandSender> parser = new RotationParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Rotation> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue().isPresent()).isTrue();
        assertThat(result.parsedValue().get().apply(new Location(null, 0, 0, 0, 10, 20))).isEqualTo(expectedLocation);
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    static @NonNull Stream<@NonNull Arguments> Parse_HappyFlow_Success_Source() {
        return Stream.of(
                arguments("~ ~", new Location(null, 0, 0, 0, 10, 20)),
                arguments("~10 ~10", new Location(null, 0, 0, 0, 20, 30)),
                arguments("~-10 ~-10", new Location(null, 0, 0, 0, 0, 10)),
                arguments("~0.5 ~-0.5", new Location(null, 0, 0, 0, 10.5f, 19.5f)),
                arguments("~-0.5 ~0.5", new Location(null, 0, 0, 0, 9.5f, 20.5f)),
                arguments("0 0", new Location(null, 0, 0, 0, 0, 0)),
                arguments("10 10", new Location(null, 0, 0, 0, 10, 10)),
                arguments("-10 -10", new Location(null, 0, 0, 0, -10, -10)),
                arguments("0.5 -0.5", new Location(null, 0, 0, 0, 0.5f, -0.5f)),
                arguments("-0.5 0.5", new Location(null, 0, 0, 0, -0.5f, 0.5f))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"^ ^", "0 ^", "0", "0~", "0 not an angle", "not an angle"})
    void Parse_InvalidAngle_Failure(final @NonNull String input) {
        // Arrange
        final RotationParser<CommandSender> parser = new RotationParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Rotation> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.failure()).isPresent();
        assertThat(result.parsedValue()).isEmpty();
    }

}
