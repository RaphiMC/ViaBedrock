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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

public class UnsignedVarBigIntegerType extends Type<BigInteger> {

    private static final BigInteger BIG_INTEGER_7F = BigInteger.valueOf(0x7F);
    private static final BigInteger BIG_INTEGER_80 = BigInteger.valueOf(0x80);

    public UnsignedVarBigIntegerType() {
        super("UnsignedVarBigInteger", BigInteger.class);
    }

    @Override
    public BigInteger read(ByteBuf buffer) {
        BigInteger val = BigInteger.ZERO;
        int shift = 0;
        byte in;
        do {
            in = buffer.readByte();
            val = val.or(BigInteger.valueOf(in & 0x7F).shiftLeft(shift));
            shift += 7;
        } while ((in & 0x80) != 0);
        return val;
    }

    @Override
    public void write(ByteBuf buffer, BigInteger value) {
        while (value.compareTo(BIG_INTEGER_7F) > 0) {
            buffer.writeByte(value.and(BIG_INTEGER_7F).or(BIG_INTEGER_80).byteValue());
            value = value.shiftRight(7);
        }
        buffer.writeByte(value.byteValue());
    }

}
