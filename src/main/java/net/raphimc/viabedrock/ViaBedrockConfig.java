/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.util.Config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ViaBedrockConfig extends Config implements net.raphimc.viabedrock.platform.ViaBedrockConfig {

    private boolean blobCacheEnabled;
    private String resourcePackHost;
    private int resourcePackPort;
    private String resourcePackUrl;
    private boolean storePacks;

    public ViaBedrockConfig(final File configFile) {
        super(configFile);
        Via.getManager().getConfigurationProvider().register(this);
    }

    @Override
    public void reload() {
        super.reload();
        this.loadFields();
    }

    private void loadFields() {
        this.blobCacheEnabled = this.getBoolean("blob-cache", true);
        this.resourcePackHost = this.getString("resource-pack-host", "127.0.0.1");
        this.resourcePackPort = this.getInt("resource-pack-port", 0);
        this.resourcePackUrl = this.getString("resource-pack-url", "");
        this.storePacks = this.getBoolean("store-packs", true);
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
    public boolean isBlobCacheEnabled() {
        return this.blobCacheEnabled;
    }

    @Override
    public String getResourcePackHost() {
        return this.resourcePackHost;
    }

    @Override
    public int getResourcePackPort() {
        return this.resourcePackPort;
    }

    public String getResourcePackUrl() {
        return this.resourcePackUrl;
    }

    @Override
    public boolean storePacks() {
        return this.storePacks;
    }

}
