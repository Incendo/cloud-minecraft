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
package org.incendo.cloud.bukkit.parser.location;

import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Parser that parses {@link Location2D} from two doubles. This will use the command
 * senders world when it exists, or else it'll use the first loaded Bukkit world
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
public final class Location2DParser<C> implements ArgumentParser<C, Location2D>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new location 2D parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Location2D> location2DParser() {
        return ParserDescriptor.of(new Location2DParser<>(), Location2D.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #location2DParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Location2D> location2DComponent() {
        return CommandComponent.<C, Location2D>builder().parser(location2DParser());
    }

    private final LocationCoordinateParser<C> locationCoordinateParser = new LocationCoordinateParser<>();

    @Override
    public @NonNull ArgumentParseResult<@NonNull Location2D> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (commandInput.remainingTokens() < 2) {
            return ArgumentParseResult.failure(
                    new LocationParser.LocationParseException(
                            commandContext,
                            LocationParser.LocationParseException.FailureReason.WRONG_FORMAT,
                            commandInput.remainingInput()
                    )
            );
        }
        final LocationCoordinate[] coordinates = new LocationCoordinate[2];
        for (int i = 0; i < 2; i++) {
            if (commandInput.peekString().isEmpty()) {
                return ArgumentParseResult.failure(
                        new LocationParser.LocationParseException(
                                commandContext,
                                LocationParser.LocationParseException.FailureReason.WRONG_FORMAT,
                                commandInput.remainingInput()
                        )
                );
            }
            final ArgumentParseResult<@NonNull LocationCoordinate> coordinate = this.locationCoordinateParser.parse(
                    commandContext,
                    commandInput
            );
            if (coordinate.failure().isPresent()) {
                return ArgumentParseResult.failure(
                        coordinate.failure().get()
                );
            }
            coordinates[i] = coordinate.parsedValue().orElseThrow(NullPointerException::new);
        }
        final Location originalLocation;
        final CommandSender bukkitSender = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);

        if (bukkitSender instanceof BlockCommandSender) {
            originalLocation = ((BlockCommandSender) bukkitSender).getBlock().getLocation();
        } else if (bukkitSender instanceof Entity) {
            originalLocation = ((Entity) bukkitSender).getLocation();
        } else {
            originalLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        }

        if (coordinates[0].type() == LocationCoordinateType.LOCAL && coordinates[1].type() != LocationCoordinateType.LOCAL) {
            return ArgumentParseResult.failure(
                    new LocationParser.LocationParseException(
                            commandContext,
                            LocationParser.LocationParseException.FailureReason.MIXED_LOCAL_ABSOLUTE,
                            ""
                    )
            );
        }

        if (coordinates[0].type() == LocationCoordinateType.ABSOLUTE) {
            originalLocation.setX(coordinates[0].coordinate());
        } else if (coordinates[0].type() == LocationCoordinateType.RELATIVE) {
            originalLocation.add(coordinates[0].coordinate(), 0, 0);
        }

        if (coordinates[1].type() == LocationCoordinateType.ABSOLUTE) {
            originalLocation.setZ(coordinates[1].coordinate());
        } else if (coordinates[1].type() == LocationCoordinateType.RELATIVE) {
            originalLocation.add(0, 0, coordinates[1].coordinate());
        } else {
            final Vector declaredPos = new Vector(
                    coordinates[0].coordinate(),
                    0,
                    coordinates[1].coordinate()
            );
            final Location local = LocationParser.toLocalSpace(originalLocation, declaredPos);
            return ArgumentParseResult.success(Location2D.from(
                    originalLocation.getWorld(),
                    local.getX(),
                    local.getZ()
            ));
        }

        return ArgumentParseResult.success(Location2D.from(
                originalLocation.getWorld(),
                originalLocation.getX(),
                originalLocation.getZ()
        ));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return LocationParser.getSuggestions(2, commandContext, input);
    }

}
