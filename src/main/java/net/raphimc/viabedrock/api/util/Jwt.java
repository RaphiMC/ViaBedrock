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
package net.raphimc.viabedrock.api.util;

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record Jwt(JsonObject header, JsonObject payload, byte[] signature) {

    public static Jwt parse(final String compactJwt) {
        if (compactJwt == null) {
            throw new IllegalArgumentException("JWT string is null");
        }
        final String[] parts = compactJwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("JWT must have at least header and payload");
        }
        final JsonObject header = JsonParser.parseString(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8)).getAsJsonObject();
        final JsonObject payload = JsonParser.parseString(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)).getAsJsonObject();
        final byte[] signature = parts.length > 2 ? Base64.getUrlDecoder().decode(parts[2]) : null;
        return new Jwt(header, payload, signature);
    }

}
