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
package org.incendo.cloud.bukkit.parser.rotation;

import java.util.Objects;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a rotation that can be applied to a {@link Location}.
 *
 * @since 2.0.0
 */
public final class Rotation {

    private final Angle yaw;
    private final Angle pitch;

    private Rotation(
            final @NonNull Angle yaw,
            final @NonNull Angle pitch
    ) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Create a new rotation object.
     *
     * @param yaw   yaw
     * @param pitch pitch
     * @return Created rotation instance.
     */
    public static Rotation of(
            final @NonNull Angle yaw,
            final @NonNull Angle pitch
    ) {
        return new Rotation(yaw, pitch);
    }

    /**
     * Returns the yaw of this rotation.
     *
     * @return yaw
     */
    public Angle yaw() {
        return this.yaw;
    }

    /**
     * Returns the pitch of this rotation
     *
     * @return pitch
     */
    public Angle pitch() {
        return this.pitch;
    }

    /**
     * Applies this rotation to a location.
     *
     * @param location the location to be modified
     * @return the modified location
     */
    public @NonNull Location apply(
            final @NonNull Location location
    ) {
        location.setYaw(this.yaw.apply(location.getYaw()));
        location.setPitch(this.pitch.apply(location.getPitch()));
        return location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rotation that = (Rotation) o;
        return this.yaw.equals(that.yaw) && this.pitch.equals(that.pitch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.yaw, this.pitch);
    }

    @Override
    public String toString() {
        return String.format("Rotation{yaw=%s, pitch=%s}", this.yaw, this.pitch);
    }

}
