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
package net.raphimc.viabedrock;

import com.viaversion.viaversion.util.Config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ViaBedrockConfig extends Config implements net.raphimc.viabedrock.platform.ViaBedrockConfig {

    private BlobCacheMode blobCacheMode;
    private boolean translateResourcePacks;
    private String resourcePackHost;
    private int resourcePackPort;
    private String resourcePackUrl;
    private PackCacheMode packCacheMode;
    private boolean translateShowCoordinatesGameRule;

    public ViaBedrockConfig(final File configFile, final Logger logger) {
        super(configFile, logger);
    }

    @Override
    public void reload() {
        super.reload();
        this.loadFields();
    }

    private void loadFields() {
        this.blobCacheMode = BlobCacheMode.byName(this.getString("blob-cache", "disk"));
        this.translateResourcePacks = this.getBoolean("translate-resource-packs", true);
        this.resourcePackHost = this.getString("resource-pack-host", "127.0.0.1");
        this.resourcePackPort = this.getInt("resource-pack-port", 0);
        this.resourcePackUrl = this.getString("resource-pack-url", "");
        this.packCacheMode = PackCacheMode.byName(this.getString("pack-cache", "disk"));
        this.translateShowCoordinatesGameRule = this.getBoolean("translate-show-coordinates-game-rule", false);
    }

    @Override
    public URL getDefaultConfigURL() {
        return this.getClass().getClassLoader().getResource("assets/viabedrock/viabedrock.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> map) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.emptyList();
    }

    @Override
    public BlobCacheMode getBlobCacheMode() {
        return this.blobCacheMode;
    }

    @Override
    public boolean shouldTranslateResourcePacks() {
        return this.translateResourcePacks;
    }

    @Override
    public String getResourcePackHost() {
        return this.resourcePackHost;
    }

    @Override
    public int getResourcePackPort() {
        return this.resourcePackPort;
    }

    @Override
    public String getResourcePackUrl() {
        return this.resourcePackUrl;
    }

    @Override
    public PackCacheMode getPackCacheMode() {
        return this.packCacheMode;
    }

    @Override
    public boolean shouldTranslateShowCoordinatesGameRule() {
        return this.translateShowCoordinatesGameRule;
    }

}
