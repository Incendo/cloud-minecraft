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
package org.incendo.cloud.minestom;

import java.util.function.BiPredicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minestom.caption.MinestomDefaultCaptionsProvider;
import org.incendo.cloud.minestom.parser.DimensionTypeParser;
import org.incendo.cloud.minestom.parser.EntityTypeParser;
import org.incendo.cloud.minestom.parser.GameModeParser;
import org.incendo.cloud.minestom.parser.InstanceParser;
import org.incendo.cloud.minestom.parser.ItemStackParser;
import org.incendo.cloud.minestom.parser.PlayerParser;
import org.incendo.cloud.minestom.parser.location.PosParser;
import org.incendo.cloud.minestom.parser.location.VecParser;

/**
 * Command manager for the Minestom platform.
 *
 * @param <C> command sender type
 */
@SuppressWarnings("this-escape")
public final class MinestomCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSender, C> {

    private final SenderMapper<CommandSender, C> senderMapper;
    private final BiPredicate<CommandSender, String> permissionHandler;

    /**
     * Creates a new command manager.
     *
     * @param executionCoordinator execution coordinator instance
     * @param senderMapper mapper between Minestom's {@link CommandSender} and the command sender type {@code C}
     * @param permissionHandler your custom permission handler; receives the Minestom sender and permission node.
     *                          Console senders and OP-level-4 players are automatically granted permissions
     *                          before this is consulted. Minestom, why did you remove the permission methods :(
     */
    public MinestomCommandManager(
        final @NonNull ExecutionCoordinator<C> executionCoordinator,
        final @NonNull SenderMapper<CommandSender, C> senderMapper,
        final @NonNull BiPredicate<CommandSender, String> permissionHandler
    ) {
        super(executionCoordinator, new MinestomCommandRegistrationHandler<>());
        ((MinestomCommandRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);

        this.senderMapper = senderMapper;
        this.permissionHandler = permissionHandler;

        this.parserRegistry()
            .registerParser(PlayerParser.playerParser())
            .registerParser(EntityTypeParser.entityTypeParser())
            .registerParser(InstanceParser.instanceParser())
            .registerParser(GameModeParser.gameModeParser())
            .registerParser(DimensionTypeParser.dimensionTypeParser())
            .registerParser(PosParser.posParser())
            .registerParser(VecParser.vecParser())
            .registerParser(ItemStackParser.itemStackParser());

        this.captionRegistry().registerProvider(new MinestomDefaultCaptionsProvider<>());

        this.registerDefaultExceptionHandlers(
            triplet -> {
                final CommandSender minestomSender = this.senderMapper.reverse(triplet.first().sender());
                final String message = triplet.first().formatCaption(triplet.second(), triplet.third());
                minestomSender.sendMessage(Component.text(message, NamedTextColor.RED));
            },
            pair -> MinecraftServer.LOGGER.error(pair.first(), pair.second())
        );
    }

    /**
     * Creates a new command manager using a noop handler for the permission handler
     *
     * @param executionCoordinator execution coordinator
     * @param senderMapper mapper between Minestom's {@link CommandSender} and the command sender type {@code C}
     */
    public MinestomCommandManager(
        final @NonNull ExecutionCoordinator<C> executionCoordinator,
        final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) {
        this(executionCoordinator, senderMapper, (_, _) -> false);
    }

    @Override
    public boolean hasPermission(
        final @NonNull C sender,
        final @NonNull String permission
    ) {
        if (permission.isEmpty()) {
            return true;
        }

        final CommandSender minestom = this.senderMapper.reverse(sender);
        return this.permissionHandler.test(minestom, permission)
            || minestom instanceof ConsoleSender
            || (minestom instanceof Player player && player.getPermissionLevel() >= 4);
    }

    @Override
    public @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }
}
