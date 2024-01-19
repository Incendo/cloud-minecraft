package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Command;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import cloud.commandframework.types.Either;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

/**
 * Example of a command accepting {@link Either}.
 */
public final class EitherExample implements AnnotationFeature {

    private BukkitAudiences bukkitAudiences;

    @Override
    public void registerFeature(final @NonNull ExamplePlugin examplePlugin, final @NonNull AnnotationParser<CommandSender> annotationParser) {
        this.bukkitAudiences = examplePlugin.bukkitAudiences();
        annotationParser.parse(this);
    }

    @Command("annotations either <uuid>")
    public void eitherCommand(final @NonNull CommandSender sender, final @NonNull Either<UUID, Player> uuid) {
        final UUID resolvedUuid = uuid.primary().orElseGet(() -> uuid.fallback().map(Player::getUniqueId).get());
        this.bukkitAudiences.sender(sender)
            .sendMessage(text("The UUID is: ", NamedTextColor.DARK_GREEN).append(text(resolvedUuid.toString(), NamedTextColor.GREEN)));
    }
}
