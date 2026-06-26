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
import net.raphimc.viabedrock.api.util.Jwt;

import java.security.KeyPair;
import java.util.UUID;

public class AuthData implements StorableObject {

    private final String multiplayerToken;
    private final Jwt multiplayerTokenJwt;
    private final KeyPair sessionKeyPair;

    private UUID deviceId;
    private UUID selfSignedId;
    private Long clientRandomId;
    private String skinJwt;

    public AuthData(final String multiplayerToken, final KeyPair sessionKeyPair) {
        this.multiplayerToken = multiplayerToken;
        this.multiplayerTokenJwt = Jwt.parse(multiplayerToken);
        this.sessionKeyPair = sessionKeyPair;
    }

    public AuthData(final String multiplayerToken, final KeyPair sessionKeyPair, final UUID deviceId) {
        this(multiplayerToken, sessionKeyPair);
        this.deviceId = deviceId;
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

    public void setDeviceId(final UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getSelfSignedId() {
        return this.selfSignedId;
    }

    public void setSelfSignedId(final UUID selfSignedId) {
        this.selfSignedId = selfSignedId;
    }

    public Long getClientRandomId() {
        return this.clientRandomId;
    }

    public void setClientRandomId(final Long clientRandomId) {
        this.clientRandomId = clientRandomId;
    }

    public String getSkinJwt() {
        return this.skinJwt;
    }

    public void setSkinJwt(final String skinJwt) {
        this.skinJwt = skinJwt;
    }

    public String getDisplayName() {
        return this.multiplayerTokenJwt.payload().get("xname").getAsString();
    }

    public String getXuid() {
        return this.multiplayerTokenJwt.payload().get("xid").getAsString();
    }

    @Deprecated(forRemoval = true)
    public AuthData(final String mojangJwt, final String identityJwt, final String multiplayerToken, final KeyPair sessionKeyPair, final UUID deviceId) {
        this(multiplayerToken, sessionKeyPair, deviceId);
    }

}
