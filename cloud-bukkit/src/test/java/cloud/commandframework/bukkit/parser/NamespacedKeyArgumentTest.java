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
package cloud.commandframework.bukkit.parser;

import cloud.commandframework.bukkit.BukkitCommandManager;
import org.bukkit.NamespacedKey;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.StandardCommandContextFactory;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation")
class NamespacedKeyArgumentTest {

    @Mock
    private BukkitCommandManager<Object> commandManager;

    private CommandContext<Object> commandContext;

    @BeforeEach
    void setup() {
        this.commandContext = new StandardCommandContextFactory<>(this.commandManager).create(
                false /* suggestions */,
                new Object()
        );
    }

    @Test
    void RequireExplicitNamespace_NamespaceProvided_Succeeds() {
        // Arrange
        final NamespacedKeyParser<Object> parser = new NamespacedKeyParser<>(
                true /* requireExplicitNamespace */,
                NamespacedKey.MINECRAFT
        );
        final CommandInput commandInput = CommandInput.of("minecraft:test");

        // Act
        final ArgumentParseResult<NamespacedKey> result = parser.parse(
                this.commandContext,
                commandInput
        );

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue()).hasValue(new NamespacedKey(NamespacedKey.MINECRAFT, "test"));
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    @Test
    void RequireExplicitNamespace_NamespaceMissing_Fails() {
        // Arrange
        final NamespacedKeyParser<Object> parser = new NamespacedKeyParser<>(
                true /* requireExplicitNamespace */,
                NamespacedKey.MINECRAFT
        );
        final CommandInput commandInput = CommandInput.of("test");

        // Act
        final ArgumentParseResult<NamespacedKey> result = parser.parse(
                this.commandContext,
                commandInput
        );

        // Assert
        assertThat(result.failure()).isPresent();
        assertThat(result.parsedValue()).isEmpty();
    }

    @Test
    void RequireExplicitNamespaceFalse_NamespaceMissing_Succeeds() {
        // Arrange
        final NamespacedKeyParser<Object> parser = new NamespacedKeyParser<>(
                false /* requireExplicitNamespace */,
                NamespacedKey.MINECRAFT
        );
        final CommandInput commandInput = CommandInput.of("test");

        // Act
        final ArgumentParseResult<NamespacedKey> result = parser.parse(
                this.commandContext,
                commandInput
        );

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue()).hasValue(new NamespacedKey(NamespacedKey.MINECRAFT, "test"));
        assertThat(commandInput.remainingInput()).isEmpty();
    }
}
