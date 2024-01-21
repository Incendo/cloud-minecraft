//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.sponge;

import cloud.commandframework.SenderMapper;
import cloud.commandframework.execution.ExecutionCoordinator;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.util.Types;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;

/**
 * Injection module that allows for {@link SpongeCommandManager} to be injectable.
 *
 * @param <C> Command sender type
 */
public final class CloudInjectionModule<C> extends AbstractModule {

    private final Class<C> commandSenderType;
    private final ExecutionCoordinator<C> executionCoordinator;
    private final SenderMapper<@NonNull CommandCause, @NonNull C> senderMapper;

    /**
     * Create a new injection module.
     *
     * @param commandSenderType    Your command sender type
     * @param executionCoordinator Command execution coordinator
     * @param senderMapper         Function mapping the custom command sender type to a Sponge CommandCause
     */
    public CloudInjectionModule(
        final @NonNull Class<C> commandSenderType,
        final @NonNull ExecutionCoordinator<C> executionCoordinator,
        final @NonNull SenderMapper<@NonNull CommandCause, @NonNull C> senderMapper
    ) {
        this.commandSenderType = commandSenderType;
        this.executionCoordinator = executionCoordinator;
        this.senderMapper = senderMapper;
    }

    /**
     * Create a new injection module using Sponge's {@link CommandCause} as the sender type.
     *
     * @param executionCoordinator Command execution coordinator
     * @return new injection module
     */
    public static @NonNull CloudInjectionModule<@NonNull CommandCause> createNative(
        final @NonNull ExecutionCoordinator<CommandCause> executionCoordinator
    ) {
        return new CloudInjectionModule<>(
            CommandCause.class,
            executionCoordinator,
            SenderMapper.identity()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void configure() {
        final Type commandExecutionCoordinatorType = Types.newParameterizedType(
            ExecutionCoordinator.class, this.commandSenderType
        );
        final Key coordinatorKey = Key.get(commandExecutionCoordinatorType);
        this.bind(coordinatorKey).toInstance(this.executionCoordinator);

        final Type commandSenderMapperFunction = Types.newParameterizedType(
            SenderMapper.class, CommandCause.class, this.commandSenderType
        );
        final Key senderMapperKey = Key.get(commandSenderMapperFunction);
        this.bind(senderMapperKey).toInstance(this.senderMapper);
    }

}
