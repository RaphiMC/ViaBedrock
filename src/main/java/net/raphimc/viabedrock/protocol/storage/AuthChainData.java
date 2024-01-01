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

import com.viaversion.viaversion.api.connection.StorableObject;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;

public class AuthChainData implements StorableObject {

    private final String mojangJwt;
    private final String identityJwt;
    private final ECPublicKey publicKey;
    private final ECPrivateKey privateKey;
    private final UUID deviceId;
    private final String playFabId;

    private String selfSignedJwt;
    private String skinJwt;
    private String displayName;
    private UUID identity;
    private String xuid;

    public AuthChainData(final String mojangJwt, final String identityJwt, final ECPublicKey publicKey, final ECPrivateKey privateKey, final UUID deviceId, final String playFabId) {
        this.mojangJwt = mojangJwt;
        this.identityJwt = identityJwt;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.deviceId = deviceId;
        this.playFabId = playFabId;
    }

    public String getMojangJwt() {
        return this.mojangJwt;
    }

    public String getIdentityJwt() {
        return this.identityJwt;
    }

    public ECPublicKey getPublicKey() {
        return this.publicKey;
    }

    public ECPrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public UUID getDeviceId() {
        return this.deviceId;
    }

    public String getPlayFabId() {
        return this.playFabId;
    }

    public String getSelfSignedJwt() {
        return this.selfSignedJwt;
    }

    public void setSelfSignedJwt(final String selfSignedJwt) {
        this.selfSignedJwt = selfSignedJwt;
    }

    public String getSkinJwt() {
        return this.skinJwt;
    }

    public void setSkinJwt(final String skinJwt) {
        this.skinJwt = skinJwt;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public UUID getIdentity() {
        return this.identity;
    }

    public void setIdentity(final UUID identity) {
        this.identity = identity;
    }

    public String getXuid() {
        return this.xuid;
    }

    public void setXuid(final String xuid) {
        this.xuid = xuid;
    }

}
