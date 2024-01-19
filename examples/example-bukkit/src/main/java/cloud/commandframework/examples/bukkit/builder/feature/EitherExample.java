package cloud.commandframework.examples.bukkit.builder.feature;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.UUIDParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.parser.PlayerParser;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.types.Either;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

/**
 * Example of a command accepting {@link Either}.
 */
public final class EitherExample implements BuilderFeature {

    private static final CloudKey<Either<UUID, Player>> EITHER_KEY = CloudKey.of(
        "uuid",
        new TypeToken<Either<UUID, Player>>() {}
    );

    @Override
    public void registerFeature(final @NonNull ExamplePlugin examplePlugin, final @NonNull BukkitCommandManager<CommandSender> manager) {
        manager.command(
            manager.commandBuilder("builder")
                .literal("either")
                .required(EITHER_KEY, ArgumentParser.firstOf(UUIDParser.uuidParser(), PlayerParser.playerParser()))
                .handler(context -> {
                    final Either<UUID, Player> either = context.get(EITHER_KEY);
                    final UUID uuid = either.primary().orElseGet(() -> either.fallback().map(Player::getUniqueId).get());
                    examplePlugin.bukkitAudiences()
                        .sender(context.sender())
                        .sendMessage(text("The UUID is: ", NamedTextColor.DARK_GREEN).append(text(uuid.toString(), NamedTextColor.GREEN)));
                })
        );
    }
}
