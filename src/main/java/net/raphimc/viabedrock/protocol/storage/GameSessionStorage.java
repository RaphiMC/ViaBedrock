/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntPair;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.api.io.compression.ProtocolCompression;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.JavaRegistries;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ChatRestrictionLevel;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.GameType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameSessionStorage extends StoredObject {

    private ProtocolCompression protocolCompression;

    private CompoundTag javaRegistries;
    private CompoundTag bedrockBiomeDefinitions = BedrockProtocol.MAPPINGS.getBedrockBiomeDefinitions();
    private final Map<String, IntIntPair> bedrockDimensionDefinitions = new HashMap<>();
    private final Set<String> availableEntityIdentifiers = new HashSet<>(BedrockProtocol.MAPPINGS.getBedrockEntities().keySet());
    private Semver bedrockVanillaVersion;
    private boolean flatGenerator;
    private int movementRewindHistorySize;
    private GameType levelGameType;
    private long levelTime;
    private boolean hardcoreMode;
    private ChatRestrictionLevel chatRestrictionLevel;
    private boolean commandsEnabled;
    private boolean inventoryServerAuthoritative;
    private boolean blockBreakingServerAuthoritative;

    private boolean immutableWorld;
    private TextComponent deathMessage;

    public GameSessionStorage(final UserConnection user) {
        super(user);

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
            this.javaRegistries = JavaRegistries.createJavaRegistries(this, this.user().get(ResourcePacksStorage.class));
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

    public Set<String> getAvailableEntityIdentifiers() {
        return this.availableEntityIdentifiers;
    }

    public void addEntityIdentifier(final String entityIdentifier) {
        this.availableEntityIdentifiers.add(entityIdentifier);
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

    public int getMovementRewindHistorySize() {
        return this.movementRewindHistorySize;
    }

    public void setMovementRewindHistorySize(final int movementRewindHistorySize) {
        this.movementRewindHistorySize = movementRewindHistorySize;
    }

    public GameType getLevelGameType() {
        return this.levelGameType;
    }

    public void setLevelGameType(final GameType levelGameType) {
        this.levelGameType = levelGameType;
    }

    public long getLevelTime() {
        return this.levelTime;
    }

    public void setLevelTime(final long levelTime) {
        this.levelTime = levelTime;
    }

    public boolean isHardcoreMode() {
        return this.hardcoreMode;
    }

    public void setHardcoreMode(final boolean hardcoreMode) {
        this.hardcoreMode = hardcoreMode;
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

    public boolean isInventoryServerAuthoritative() {
        return this.inventoryServerAuthoritative;
    }

    public void setInventoryServerAuthoritative(final boolean inventoryServerAuthoritative) {
        this.inventoryServerAuthoritative = inventoryServerAuthoritative;
    }

    public boolean isBlockBreakingServerAuthoritative() {
        return this.blockBreakingServerAuthoritative;
    }

    public void setBlockBreakingServerAuthoritative(final boolean blockBreakingServerAuthoritative) {
        this.blockBreakingServerAuthoritative = blockBreakingServerAuthoritative;
    }

    public boolean isImmutableWorld() {
        return this.immutableWorld;
    }

    public void setImmutableWorld(final boolean immutableWorld) {
        this.immutableWorld = immutableWorld;
    }

    public TextComponent getDeathMessage() {
        return this.deathMessage;
    }

    public void setDeathMessage(final TextComponent deathMessage) {
        this.deathMessage = deathMessage;
    }

}
