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
package net.raphimc.viabedrock.netty;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class AesEncryptionCodec extends ByteToMessageCodec<ByteBuf> {

    private final SecretKey secretKey;
    private final Cipher inCipher;
    private final Cipher outCipher;
    private final MessageDigest sha256;
    private long sentPacketCounter;
    private long receivedPacketCounter;

    public AesEncryptionCodec(final SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
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
        final byte[] hash = this.generateHash(in, this.sentPacketCounter++);

        final ByteBuffer inBuffer = in.nioBuffer();
        out.ensureWritable(in.readableBytes() + 8);

        this.outCipher.update(inBuffer, out.nioBuffer(0, in.readableBytes()));
        this.outCipher.update(ByteBuffer.wrap(hash), out.nioBuffer(in.readableBytes(), 8));
        out.writerIndex(in.readableBytes() + 8);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final ByteBuffer inBuffer = in.nioBuffer();
        final ByteBuffer outBuffer = inBuffer.duplicate();

        this.inCipher.update(inBuffer, outBuffer);
        final ByteBuf output = in.readRetainedSlice(in.readableBytes() - 8);

        final byte[] hash = new byte[8];
        in.readBytes(hash);
        final byte[] expectedHash = this.generateHash(output, this.receivedPacketCounter++);
        if (!Arrays.equals(expectedHash, hash)) {
            throw new CorruptedFrameException("Invalid encrypted packet");
        }

        out.add(output);
    }

    private byte[] generateHash(final ByteBuf buf, final long packetCounter) {
        this.sha256.update(Longs.toByteArray(Long.reverseBytes(packetCounter)));
        this.sha256.update(ByteBufUtil.getBytes(buf));
        this.sha256.update(this.secretKey.getEncoded());
        final byte[] hash = this.sha256.digest();
        this.sha256.reset();
        return Arrays.copyOf(hash, 8);
    }

}
