/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.platform;

import com.viaversion.viaversion.api.configuration.Config;
import net.raphimc.viabedrock.protocol.provider.BlobCacheProvider;
import net.raphimc.viabedrock.protocol.provider.ResourcePackProvider;
import net.raphimc.viabedrock.protocol.provider.impl.*;

import java.util.function.Supplier;

public interface ViaBedrockConfig extends Config {

    /**
     * @return The blob cache mode to use.
     */
    BlobCacheMode getBlobCacheMode();

    /**
     * @return If true, starts the resource pack HTTP server and enables resource pack translation
     */
    boolean shouldTranslateResourcePacks();

    /**
     * @return The host to use for the resource pack HTTP server.
     */
    String getResourcePackHost();

    /**
     * @return The port to use for the resource pack HTTP server.
     */
    int getResourcePackPort();

    /**
     * @return The URL to use for the resource pack HTTP server.
     */
    String getResourcePackUrl();

    /**
     * @return The pack cache mode to use.
     */
    PackCacheMode getPackCacheMode();

    /**
     * @return If true, translates bedrock's showCoordinates game rule to java's reduced debug info flag
     */
    boolean shouldTranslateShowCoordinatesGameRule();

    enum BlobCacheMode {

        /**
         * The blob cache will be disabled.
         */
        DISABLED(NoOpBlobCacheProvider::new),
        /**
         * The blob cache will be enabled and blobs will be stored in memory.
         */
        MEMORY(InMemoryBlobCacheProvider::new),
        /**
         * The blob cache will be enabled and blobs will be stored on disk.
         */
        DISK(DiskBlobCacheProvider::new);

        private final Supplier<BlobCacheProvider> providerSupplier;

        BlobCacheMode(final Supplier<BlobCacheProvider> providerSupplier) {
            this.providerSupplier = providerSupplier;
        }

        public static BlobCacheMode byName(String name) {
            for (BlobCacheMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }

            return DISABLED;
        }

        public BlobCacheProvider createProvider() {
            return this.providerSupplier.get();
        }

    }

    enum PackCacheMode {

        /**
         * The pack cache will be disabled.
         */
        DISABLED(NoOpResourcePackProvider::new),
        /**
         * The pack cache will be enabled and packs will be stored in memory.
         */
        MEMORY(InMemoryResourcePackProvider::new),
        /**
         * The pack cache will be enabled and packs will be stored on disk.
         */
        DISK(DiskResourcePackProvider::new);

        private final Supplier<ResourcePackProvider> providerSupplier;

        PackCacheMode(final Supplier<ResourcePackProvider> providerSupplier) {
            this.providerSupplier = providerSupplier;
        }

        public static PackCacheMode byName(String name) {
            for (PackCacheMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }

            return DISABLED;
        }

        public ResourcePackProvider createProvider() {
            return this.providerSupplier.get();
        }

    }

}
