/**
 * Brigadier mappings.
 *
 * <p>For platform implementations using Brigadier, {@link org.incendo.cloud.brigadier.CloudBrigadierManager} can map
 * Cloud {@link org.incendo.cloud.CommandTree command trees} to Brigadier nodes.</p>
 *
 * <p>To bridge Brigadier and Cloud argument types, an argument parser that wraps Brigadier argument types is available in
 * {@link org.incendo.cloud.brigadier.parser.WrappedBrigadierParser}. Other classes in that package allow constructing
 * Brigadier {@link com.mojang.brigadier.StringReader} instances that can be used for efficient interoperability with
 * Brigadier.</p>
 */
package org.incendo.cloud.brigadier;
