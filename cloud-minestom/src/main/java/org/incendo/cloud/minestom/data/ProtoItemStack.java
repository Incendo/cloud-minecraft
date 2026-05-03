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
package org.incendo.cloud.minestom.data;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Intermediary result for an argument which parses a {@link net.minestom.server.item.Material} and optional NBT data.
 *
 * @since 2.1.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public final class ProtoItemStack {

    private final Material material;

    /**
     * Creates a new proto item stack.
     *
     * @param material the material
     */
    public ProtoItemStack(final @NonNull Material material) {
        this.material = material;
    }

    /**
     * Creates an {@link ItemStack} from this proto item stack with the given amount.
     *
     * @param amount the stack size
     * @return the created item stack
     * @throws IllegalArgumentException if the amount exceeds the material's max stack size
     */
    public @NonNull ItemStack createItemStack(final int amount) {
        if (amount > this.material.maxStackSize()) {
            throw new IllegalArgumentException(String.format(
                "The maximum stack size for %s is %d",
                this.material.name(),
                this.material.maxStackSize()
            ));
        }
        return ItemStack.of(this.material, amount);
    }
}
