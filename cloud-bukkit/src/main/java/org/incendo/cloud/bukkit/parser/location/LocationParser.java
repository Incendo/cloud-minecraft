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

import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.type.range.Range;

/**
 * Parser that parses {@link Location} from three doubles. This will use the command
 * senders world when it exists, or else it'll use the first loaded Bukkit world.
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class LocationParser<C> implements ArgumentParser<C, Location>, BlockingSuggestionProvider.Strings<C> {

    private static final Range<Integer> SUGGESTION_RANGE = Range.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * Creates a new location parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Location> locationParser() {
        return ParserDescriptor.of(new LocationParser<>(), Location.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #locationParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Location> locationComponent() {
        return CommandComponent.<C, Location>builder().parser(locationParser());
    }

    private final LocationCoordinateParser<C> locationCoordinateParser = new LocationCoordinateParser<>();

    @Override
    public @NonNull ArgumentParseResult<@NonNull Location> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (commandInput.remainingTokens() < 3) {
            return ArgumentParseResult.failure(
                    new LocationParseException(
                            commandContext,
                            LocationParseException.FailureReason.WRONG_FORMAT,
                            commandInput.remainingInput()
                    )
            );
        }
        final CommandInput originalInput = commandInput.copy();
        final LocationCoordinate[] coordinates = new LocationCoordinate[3];
        for (int i = 0; i < 3; i++) {
            if (commandInput.peekString().isEmpty()) {
                return ArgumentParseResult.failure(
                        new LocationParseException(
                                commandContext,
                                LocationParseException.FailureReason.WRONG_FORMAT,
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
            if (Bukkit.getWorlds().isEmpty()) {
                // For testing
                originalLocation = new Location(null, 0, 0, 0);
            } else {
                originalLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
            }
        }

        if (((coordinates[0].type() == LocationCoordinateType.LOCAL)
                != (coordinates[1].type() == LocationCoordinateType.LOCAL))
                || ((coordinates[0].type() == LocationCoordinateType.LOCAL)
                != (coordinates[2].type() == LocationCoordinateType.LOCAL))
        ) {
            return ArgumentParseResult.failure(
                    new LocationParseException(
                            commandContext,
                            LocationParseException.FailureReason.MIXED_LOCAL_ABSOLUTE,
                            originalInput.remainingInput()
                    )
            );
        }

        if (coordinates[0].type() == LocationCoordinateType.ABSOLUTE) {
            originalLocation.setX(coordinates[0].coordinate());
        } else if (coordinates[0].type() == LocationCoordinateType.RELATIVE) {
            originalLocation.add(coordinates[0].coordinate(), 0, 0);
        }

        if (coordinates[1].type() == LocationCoordinateType.ABSOLUTE) {
            originalLocation.setY(coordinates[1].coordinate());
        } else if (coordinates[1].type() == LocationCoordinateType.RELATIVE) {
            originalLocation.add(0, coordinates[1].coordinate(), 0);
        }

        if (coordinates[2].type() == LocationCoordinateType.ABSOLUTE) {
            originalLocation.setZ(coordinates[2].coordinate());
        } else if (coordinates[2].type() == LocationCoordinateType.RELATIVE) {
            originalLocation.add(0, 0, coordinates[2].coordinate());
        } else {
            final Vector declaredPos = new Vector(
                    coordinates[0].coordinate(),
                    coordinates[1].coordinate(),
                    coordinates[2].coordinate()
            );
            return ArgumentParseResult.success(
                    toLocalSpace(originalLocation, declaredPos)
            );
        }

        return ArgumentParseResult.success(
                originalLocation
        );
    }

    static @NonNull Location toLocalSpace(final @NonNull Location originalLocation, final @NonNull Vector declaredPos) {
        final double cosYaw = Math.cos(toRadians(originalLocation.getYaw() + 90.0F));
        final double sinYaw = Math.sin(toRadians(originalLocation.getYaw() + 90.0F));
        final double cosPitch = Math.cos(toRadians(-originalLocation.getPitch()));
        final double sinPitch = Math.sin(toRadians(-originalLocation.getPitch()));
        final double cosNegYaw = Math.cos(toRadians(-originalLocation.getPitch() + 90.0F));
        final double sinNegYaw = Math.sin(toRadians(-originalLocation.getPitch() + 90.0F));
        final Vector zModifier = new Vector(cosYaw * cosPitch, sinPitch, sinYaw * cosPitch);
        final Vector yModifier = new Vector(cosYaw * cosNegYaw, sinNegYaw, sinYaw * cosNegYaw);
        final Vector xModifier = zModifier.crossProduct(yModifier).multiply(-1);
        final double xOffset = dotProduct(declaredPos, xModifier.getX(), yModifier.getX(), zModifier.getX());
        final double yOffset = dotProduct(declaredPos, xModifier.getY(), yModifier.getY(), zModifier.getY());
        final double zOffset = dotProduct(declaredPos, xModifier.getZ(), yModifier.getZ(), zModifier.getZ());
        return originalLocation.add(xOffset, yOffset, zOffset);
    }

    private static double dotProduct(final Vector location, final double x, final double y, final double z) {
        return location.getX() * x + location.getY() * y + location.getZ() * z;
    }

    private static float toRadians(final float degrees) {
        return degrees * (float) Math.PI / 180f;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return LocationParser.getSuggestions(3, commandContext, input);
    }

    static <C> @NonNull List<@NonNull String> getSuggestions(
            final int components,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        final CommandInput inputCopy = input.copy();

        int idx = input.cursor();
        for (int i = 0; i < components; i++) {
            idx = input.cursor();
            if (!input.hasRemainingInput(true)) {
                break;
            }
            final ArgumentParseResult<LocationCoordinate> coordinateResult = new LocationCoordinateParser<C>().parse(
                commandContext,
                input
            );
            if (coordinateResult.failure().isPresent()) {
                break;
            }
        }
        input.cursor(idx);

        if (input.hasRemainingInput() && (input.peek() == '~' || input.peek() == '^')) {
            input.read();
        }

        final String prefix = inputCopy.difference(input, true);

        return IntegerParser.getSuggestions(
            SUGGESTION_RANGE,
            input
        ).stream().map(string -> prefix + string).collect(Collectors.toList());
    }


    public static final class LocationParseException extends ParserException {

        private final String input;
        private final FailureReason reason;

        /**
         * Construct a new LocationParseException
         *
         * @param context Command context
         * @param reason  Failure reason
         * @param input   Input
         */
        public LocationParseException(
                final @NonNull CommandContext<?> context,
                final @NonNull FailureReason reason,
                final @NonNull String input
        ) {
            super(
                    LocationParser.class,
                    context,
                    reason.caption(),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
            this.reason = reason;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String input() {
            return this.input;
        }

        /**
         * Get the failure reason
         *
         * @return Failure reason
         */
        public @NonNull FailureReason reason() {
            return this.reason;
        }


        /**
         * Reasons for which location parsing may fail
         */
        public enum FailureReason {

            WRONG_FORMAT(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT),
            MIXED_LOCAL_ABSOLUTE(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE);


            private final Caption caption;

            FailureReason(final @NonNull Caption caption) {
                this.caption = caption;
            }

            /**
             * Returns the caption used for this failure reason.
             *
             * @return the caption
             */
            public @NonNull Caption caption() {
                return this.caption;
            }
        }
    }
}
