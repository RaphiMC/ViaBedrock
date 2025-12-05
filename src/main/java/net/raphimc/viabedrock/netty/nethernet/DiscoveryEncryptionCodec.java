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
package net.raphimc.viabedrock.netty.nethernet;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DiscoveryEncryptionCodec extends ByteToMessageCodec<ByteBuf> {

    private final SecretKey cryptoKey;
    private final SecretKey signingKey;

    public DiscoveryEncryptionCodec(final long applicationId) throws NoSuchAlgorithmException {
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final byte[] key = sha256.digest(Longs.toByteArray(Long.reverseBytes(applicationId)));
        this.cryptoKey = new SecretKeySpec(key, "AES");
        this.signingKey = new SecretKeySpec(key, "HmacSHA256");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        final ByteBuf payloadBuffer = Unpooled.buffer(Short.BYTES + in.readableBytes());
        payloadBuffer.writeShortLE(Short.BYTES + in.readableBytes());
        payloadBuffer.writeBytes(in);
        final byte[] payloadBytes = new byte[payloadBuffer.readableBytes()];
        payloadBuffer.readBytes(payloadBytes);
        payloadBuffer.release();

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(this.signingKey);
        final byte[] signature = hmac.doFinal(payloadBytes);
        out.writeBytes(signature);

        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.cryptoKey);
        final byte[] encryptedPayload = cipher.doFinal(payloadBytes);
        out.writeBytes(encryptedPayload);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final byte[] receivedSignature = new byte[32];
        in.readBytes(receivedSignature);
        final byte[] encryptedPayload = new byte[in.readableBytes()];
        in.readBytes(encryptedPayload);

        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.cryptoKey);
        final byte[] payloadBytes = cipher.doFinal(encryptedPayload);

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(this.signingKey);
        final byte[] expectedSignature = hmac.doFinal(payloadBytes);
        if (!MessageDigest.isEqual(receivedSignature, expectedSignature)) {
            throw new CorruptedFrameException("Invalid packet signature");
        }

        final ByteBuf payloadBuffer = Unpooled.wrappedBuffer(payloadBytes);
        final int length = payloadBuffer.readUnsignedShortLE() - Short.BYTES;
        if (payloadBuffer.readableBytes() < length) {
            throw new CorruptedFrameException("Invalid payload length");
        }
        out.add(payloadBuffer);
    }

}
