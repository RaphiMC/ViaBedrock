/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthData implements StorableObject {

    private final String mojangJwt;
    private final String identityJwt;
    private final String multiplayerToken;
    private final KeyPair sessionKeyPair;
    private final UUID deviceId;

    private String selfSignedJwt;
    private String skinJwt;
    private String displayName;
    private String xuid;

    public AuthData(final String mojangJwt, final String identityJwt, final String multiplayerToken, final KeyPair sessionKeyPair, final UUID deviceId) {
        this.mojangJwt = mojangJwt;
        this.identityJwt = identityJwt;
        this.multiplayerToken = multiplayerToken;
        this.sessionKeyPair = sessionKeyPair;
        this.deviceId = deviceId;
    }

    public String getMojangJwt() {
        return this.mojangJwt;
    }

    public String getIdentityJwt() {
        return this.identityJwt;
    }

    public String getMultiplayerToken() {
        return this.multiplayerToken;
    }

    public KeyPair getSessionKeyPair() {
        return this.sessionKeyPair;
    }

    public UUID getDeviceId() {
        return this.deviceId;
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

    public String getXuid() {
        return this.xuid;
    }

    public void setXuid(final String xuid) {
        this.xuid = xuid;
    }

    public List<String> getCertificateChain() {
        final List<String> chain = new ArrayList<>();
        if (this.selfSignedJwt != null) {
            chain.add(this.selfSignedJwt);
        }
        if (this.mojangJwt != null) {
            chain.add(this.mojangJwt);
        }
        if (this.identityJwt != null) {
            chain.add(this.identityJwt);
        }
        return chain;
    }

}
