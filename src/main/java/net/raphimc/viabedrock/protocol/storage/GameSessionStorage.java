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
package net.raphimc.viabedrock.protocol.storage;

import com.vdurmont.semver4j.Semver;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntPair;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import net.raphimc.viabedrock.api.io.compression.ProtocolCompression;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.JavaRegistries;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ChatRestrictionLevel;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ServerAuthMovementMode;

import java.util.HashMap;
import java.util.Map;

public class GameSessionStorage implements StorableObject {

    private ProtocolCompression protocolCompression;

    private CompoundTag javaRegistries;
    private CompoundTag bedrockBiomeDefinitions = BedrockProtocol.MAPPINGS.getBedrockBiomeDefinitions();
    private final Map<String, IntIntPair> bedrockDimensionDefinitions = new HashMap<>();
    private Semver bedrockVanillaVersion;
    private boolean flatGenerator;
    private ServerAuthMovementMode movementMode;
    private int levelGameType;
    private boolean hardcoreMode;
    private boolean sentStartGameResponsePackets;

    private ChatRestrictionLevel chatRestrictionLevel;
    private boolean commandsEnabled;
    private ATextComponent deathMessage;

    public GameSessionStorage() {
        this.bedrockDimensionDefinitions.put("minecraft:the_nether", new IntIntImmutablePair(0, 128));
    }

    public ProtocolCompression getProtocolCompression() {
        return this.protocolCompression;
    }

    public void setProtocolCompression(final ProtocolCompression protocolCompression) {
        this.protocolCompression = protocolCompression;
    }

    public CompoundTag getJavaRegistries() {
        if (this.javaRegistries == null) {
            this.javaRegistries = JavaRegistries.createJavaRegistries(this);
        }
        return this.javaRegistries;
    }

    public CompoundTag getBedrockBiomeDefinitions() {
        return this.bedrockBiomeDefinitions;
    }

    public void setBedrockBiomeDefinitions(final CompoundTag bedrockBiomeDefinitions) {
        this.bedrockBiomeDefinitions = bedrockBiomeDefinitions;
        this.javaRegistries = null;
    }

    public Map<String, IntIntPair> getBedrockDimensionDefinitions() {
        return this.bedrockDimensionDefinitions;
    }

    public void putBedrockDimensionDefinition(final String dimensionIdentifier, final IntIntPair dimensionDefinition) {
        this.bedrockDimensionDefinitions.put(dimensionIdentifier, dimensionDefinition);
        this.javaRegistries = null;
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

    public ServerAuthMovementMode getMovementMode() {
        return this.movementMode;
    }

    public void setMovementMode(final ServerAuthMovementMode movementMode) {
        this.movementMode = movementMode;
    }

    public int getLevelGameType() {
        return this.levelGameType;
    }

    public void setLevelGameType(final int levelGameType) {
        this.levelGameType = levelGameType;
    }

    public boolean isHardcoreMode() {
        return this.hardcoreMode;
    }

    public void setHardcoreMode(final boolean hardcoreMode) {
        this.hardcoreMode = hardcoreMode;
    }

    public boolean hasSentStartGameResponsePackets() {
        return this.sentStartGameResponsePackets;
    }

    public void setSentStartGameResponsePackets(final boolean sentStartGameResponsePackets) {
        this.sentStartGameResponsePackets = sentStartGameResponsePackets;
    }

    public ChatRestrictionLevel getChatRestrictionLevel() {
        return this.chatRestrictionLevel;
    }

    public void setChatRestrictionLevel(final ChatRestrictionLevel chatRestrictionLevel) {
        this.chatRestrictionLevel = chatRestrictionLevel;
    }

    public boolean areCommandsEnabled() {
        return this.commandsEnabled;
    }

    public void setCommandsEnabled(final boolean commandsEnabled) {
        this.commandsEnabled = commandsEnabled;
    }

    public ATextComponent getDeathMessage() {
        return this.deathMessage;
    }

    public void setDeathMessage(final ATextComponent deathMessage) {
        this.deathMessage = deathMessage;
    }

}
