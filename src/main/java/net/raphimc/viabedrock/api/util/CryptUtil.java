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

import io.jsonwebtoken.Locator;

import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptUtil {

    public static final KeyFactory EC_KEYFACTORY;
    public static final Locator<Key> X5U_KEY_LOCATOR = header -> ecPublicKeyFromBase64((String) header.get("x5u"));

    static {
        try {
            EC_KEYFACTORY = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not create EllipticCurve KeyFactory", e);
        }
    }

    public static ECPublicKey ecPublicKeyFromBase64(final String base64) {
        return ecPublicKeyFromBytes(Base64.getDecoder().decode(base64));
    }

    public static ECPublicKey ecPublicKeyFromBytes(final byte[] bytes) {
        try {
            return (ECPublicKey) EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Could not decode public key", e);
        }
    }

    public static KeyPair generateEcdsa384KeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final KeyPairGenerator secp384r1 = KeyPairGenerator.getInstance("EC");
        secp384r1.initialize(new ECGenParameterSpec("secp384r1"));
        return secp384r1.generateKeyPair();
    }

}
