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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents an angle that can be applied to a reference angle.
 *
 * @since 2.0.0
 */
public final class Angle {

    private final float angle;
    private final boolean relative;

    private Angle(
            final float angle,
            final boolean relative
    ) {
        this.angle = angle;
        this.relative = relative;
    }

    /**
     * Create a new angle object.
     *
     * @param angle    angle
     * @param relative whether the angle is relative
     * @return Created angle instance.
     */
    public static @NonNull Angle of(
            final float angle,
            final boolean relative
    ) {
        return new Angle(angle, relative);
    }

    /**
     * Returns the angle.
     *
     * @return angle
     */
    public float angle() {
        return this.angle;
    }

    /**
     * Returns if this angle is relative.
     *
     * @return whether the angle is relative
     */
    public boolean relative() {
        return this.relative;
    }

    /**
     * Applies this angle to a reference angle.
     *
     * @param angle the reference angle
     * @return the modified angle
     */
    public float apply(final float angle) {
        return this.relative ? this.angle + angle : this.angle;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Angle that = (Angle) o;
        return Float.compare(this.angle, that.angle) == 0 && this.relative == that.relative;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(this.angle);
        result = 31 * result + Boolean.hashCode(this.relative);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Angle{angle=%s, relative=%s}", this.angle, this.relative);
    }

}
