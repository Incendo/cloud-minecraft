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
package org.incendo.cloud.examples.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.sender.ConsoleSender;
import org.incendo.cloud.paper.sender.ModernProvidedSenderMapper;
import org.incendo.cloud.paper.sender.PlayerSender;
import org.incendo.cloud.paper.sender.Sender;
import org.incendo.cloud.setting.ManagerSetting;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public final class PluginBootstrap implements io.papermc.paper.plugin.bootstrap.PluginBootstrap {
    private PaperCommandManager.@MonotonicNonNull Bootstrapped<Sender> commandManager;

    @Override
    public void bootstrap(final BootstrapContext context) {
        final PaperCommandManager.Bootstrapped<Sender> mgr =
            PaperCommandManager.builder(ModernProvidedSenderMapper.providedSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildBootstrapped(context);

        this.commandManager = mgr;

        mgr.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        mgr.command(
            mgr.commandBuilder("new_command")
                .required("name", stringParser())
                .handler(ctx -> {
                    final String name = ctx.get("name");
                    mgr.command(
                        mgr.commandBuilder(name).handler(ctx1 -> {
                            ctx1.sender().sender().sendMessage("HI");
                        })
                    );
                })
        );
        mgr.command(
            mgr.commandBuilder("del_command")
                .required("name", stringParser())
                .handler(ctx -> {
                    final String name = ctx.get("name");
                    mgr.deleteRootCommand(name);
                })
        );
        mgr.command(
            mgr.commandBuilder("player_command")
                .senderType(PlayerSender.class)
                .handler(ctx -> {
                    final Player player = ctx.sender().sender();
                    player.sendMessage("hello player!");
                })
        );
        mgr.command(
            mgr.commandBuilder("console_command")
                .senderType(ConsoleSender.class)
                .handler(ctx -> {
                    final ConsoleCommandSender console = ctx.sender().sender();
                    console.sendMessage("hello console!");
                })
        );
    }

    @Override
    public JavaPlugin createPlugin(final PluginProviderContext context) {
        return new PaperPlugin(this.commandManager);
    }
}
