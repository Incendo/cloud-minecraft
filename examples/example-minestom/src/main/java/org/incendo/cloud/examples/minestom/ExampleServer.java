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
package org.incendo.cloud.examples.minestom;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minestom.MinestomCommandManager;
import org.incendo.cloud.minestom.parser.EntityTypeParser;
import org.incendo.cloud.minestom.parser.PlayerParser;
import org.incendo.cloud.parser.standard.StringParser;

import static net.kyori.adventure.text.Component.text;

/**
 * I got claude to pump this out because my laptop is on 8% and I just want to test this :(
 * don't slander me
 */
public final class ExampleServer {

    private ExampleServer() {}

    public static void main(final String[] args) {
        final MinecraftServer minecraftServer = MinecraftServer.init();

        final InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        final InstanceContainer instance = instanceManager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK));

        final GlobalEventHandler events = MinecraftServer.getGlobalEventHandler();
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0, 2, 0));
        });

        final MinestomCommandManager<CommandSender> manager = new MinestomCommandManager<>(
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity()
        );

        MinecraftExceptionHandler.<CommandSender>createNative()
            .defaultInvalidSyntaxHandler()
            .defaultInvalidSenderHandler()
            .defaultNoPermissionHandler()
            .defaultArgumentParsingHandler()
            .decorator(component -> text()
                .append(text("[", NamedTextColor.DARK_GRAY))
                .append(text("Example", NamedTextColor.GOLD))
                .append(text("] ", NamedTextColor.DARK_GRAY))
                .append(component)
                .build()
            )
            .registerTo(manager);

        registerCommands(manager);

        minecraftServer.start("0.0.0.0", 25565);
    }

    private static void registerCommands(final MinestomCommandManager<CommandSender> manager) {
        manager.command(
            manager.commandBuilder("hello", Description.of("Say hello"))
                .senderType(Player.class)
                .handler(ctx -> ctx.sender().sendMessage(
                    text("Hello, ").append(text(ctx.sender().getUsername(), NamedTextColor.GOLD))
                ))
        );

        manager.command(
            manager.commandBuilder("echo", Description.of("Echo a message"))
                .required("message", StringParser.greedyStringParser())
                .handler(ctx -> {
                    final String message = ctx.get("message");
                    ctx.sender().sendMessage(text(message, NamedTextColor.YELLOW));
                })
        );

        manager.command(
            manager.commandBuilder("player", Description.of("Look up an online player"))
                .required("player", PlayerParser.playerParser())
                .handler(ctx -> {
                    final Player target = ctx.get("player");
                    ctx.sender().sendMessage(
                        text("Found player: ", NamedTextColor.GRAY)
                            .append(text(target.getUsername(), NamedTextColor.AQUA))
                    );
                })
        );

        manager.command(
            manager.commandBuilder("entitytype", Description.of("Resolve an entity type"))
                .required("type", EntityTypeParser.entityTypeParser())
                .handler(ctx -> {
                    final EntityType type = ctx.get("type");
                    ctx.sender().sendMessage(
                        text("Entity type: ", NamedTextColor.GRAY)
                            .append(text(type.name(), NamedTextColor.AQUA))
                    );
                })
        );
    }
}
