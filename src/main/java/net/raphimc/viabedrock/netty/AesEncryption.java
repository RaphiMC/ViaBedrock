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
package net.raphimc.viabedrock.netty;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class AesEncryption extends ByteToMessageCodec<ByteBuf> {

    private final SecretKey secretKey;
    private final Cipher inCipher;
    private final Cipher outCipher;
    private final MessageDigest sha256;
    private long sentPacketCounter;
    private long receivedPacketCounter;

    private byte[] decryptBuffer = new byte[8192];
    private byte[] encryptBuffer = new byte[8192];

    public AesEncryption(final SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        final byte[] iv = new byte[16];
        System.arraycopy(secretKey.getEncoded(), 0, iv, 0, 12);
        iv[15] = 2;

        this.secretKey = secretKey;
        this.inCipher = Cipher.getInstance("AES/CTR/NoPadding");
        this.inCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        this.outCipher = Cipher.getInstance("AES/CTR/NoPadding");
        this.outCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        this.sha256 = MessageDigest.getInstance("SHA-256");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        final byte[] hash = this.generateHash(in);

        final byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);
        final int outLength = this.outCipher.getOutputSize(data.length);
        if (this.encryptBuffer.length < outLength) {
            this.encryptBuffer = new byte[outLength];
        }

        out.writeBytes(this.encryptBuffer, 0, this.outCipher.update(data, 0, data.length, this.encryptBuffer, 0));
        out.writeBytes(this.encryptBuffer, 0, this.outCipher.update(hash, 0, hash.length, this.encryptBuffer, 0));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final byte[] hash = new byte[8];
        final byte[] data = new byte[in.readableBytes() - hash.length];
        in.readBytes(data);
        in.readBytes(hash);

        final int outLength = this.inCipher.getOutputSize(data.length);
        if (this.decryptBuffer.length < outLength) {
            this.decryptBuffer = new byte[outLength];
        }

        final ByteBuf decrypted = ctx.alloc().buffer(outLength);
        decrypted.writeBytes(this.decryptBuffer, 0, this.inCipher.update(data, 0, data.length, this.decryptBuffer, 0));
        if (!this.verifyHash(decrypted, Arrays.copyOf(this.decryptBuffer, this.inCipher.update(hash, 0, hash.length, this.decryptBuffer, 0)))) {
            throw new IllegalStateException("Invalid packet hash");
        }

        out.add(decrypted);
    }

    private byte[] generateHash(final ByteBuf buf) {
        this.sha256.update(Longs.toByteArray(Long.reverseBytes(this.sentPacketCounter++)));
        this.sha256.update(ByteBufUtil.getBytes(buf));
        this.sha256.update(this.secretKey.getEncoded());
        final byte[] hash = this.sha256.digest();
        this.sha256.reset();
        return Arrays.copyOf(hash, 8);
    }

    private boolean verifyHash(final ByteBuf buf, final byte[] receivedHash) {
        this.sha256.update(Longs.toByteArray(Long.reverseBytes(this.receivedPacketCounter++)));
        this.sha256.update(ByteBufUtil.getBytes(buf));
        this.sha256.update(this.secretKey.getEncoded());
        final byte[] expectedHash = this.sha256.digest();
        this.sha256.reset();
        return Arrays.equals(receivedHash, Arrays.copyOf(expectedHash, 8));
    }

}
