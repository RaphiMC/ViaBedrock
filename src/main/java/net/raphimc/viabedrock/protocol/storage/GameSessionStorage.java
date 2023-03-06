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
package net.raphimc.viabedrock.protocol.storage;

import com.vdurmont.semver4j.Semver;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

public class GameSessionStorage extends StoredObject {

    private CompoundTag javaRegistries;
    private CompoundTag bedrockBiomeDefinitions;
    private Semver bedrockVanillaVersion;
    private boolean flatGenerator;
    private int movementMode;
    private boolean chatRestricted;
    private boolean commandsEnabled;
    private int levelGameType;

    public GameSessionStorage(final UserConnection user) {
        super(user);
    }

    public CompoundTag getJavaRegistries() {
        return this.javaRegistries;
    }

    public void setJavaRegistries(final CompoundTag javaRegistries) {
        this.javaRegistries = javaRegistries;
    }

    public CompoundTag getBedrockBiomeDefinitions() {
        return this.bedrockBiomeDefinitions;
    }

    public void setBedrockBiomeDefinitions(final CompoundTag bedrockBiomeDefinitions) {
        this.bedrockBiomeDefinitions = bedrockBiomeDefinitions;
    }

    public Semver getBedrockVanillaVersion() {
        return this.bedrockVanillaVersion;
    }

    public void setBedrockVanillaVersion(final Semver bedrockVanillaVersion) {
        this.bedrockVanillaVersion = bedrockVanillaVersion;
    }

    public boolean isFlatGenerator() {
        return this.flatGenerator;
    }

    public void setFlatGenerator(final boolean flatGenerator) {
        this.flatGenerator = flatGenerator;
    }

    public int getMovementMode() {
        return this.movementMode;
    }

    public void setMovementMode(final int movementMode) {
        this.movementMode = movementMode;
    }

    public boolean isChatRestricted() {
        return this.chatRestricted;
    }

    public void setChatRestricted(final boolean chatRestricted) {
        this.chatRestricted = chatRestricted;
    }

    public boolean areCommandsEnabled() {
        return this.commandsEnabled;
    }

    public void setCommandsEnabled(final boolean commandsEnabled) {
        this.commandsEnabled = commandsEnabled;
    }

    public int getLevelGameType() {
        return this.levelGameType;
    }

    public void setLevelGameType(final int levelGameType) {
        this.levelGameType = levelGameType;
    }

}
