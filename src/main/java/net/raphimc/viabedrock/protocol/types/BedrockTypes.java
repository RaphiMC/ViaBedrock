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
package net.raphimc.viabedrock.protocol.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.util.AsciiString;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;

public class BedrockTypes {

    public static final UnsignedShortLEType UNSIGNED_SHORT_LE = new UnsignedShortLEType();
    public static final FloatLEType FLOAT_LE = new FloatLEType();
    public static final LongLEType LONG_LE = new LongLEType();

    public static final VarIntType VAR_INT = new VarIntType();
    public static final UnsignedVarIntType UNSIGNED_VAR_INT = new UnsignedVarIntType();
    public static final VarLongType VAR_LONG = new VarLongType();
    public static final UnsignedVarLongType UNSIGNED_VAR_LONG = new UnsignedVarLongType();
    public static final Type<byte[]> BYTE_ARRAY = new ByteArrayType();
    public static final Type<AsciiString> ASCII_STRING = new AsciiStringType();
    public static final Type<String> STRING = new StringType();

    public static final Type<String[]> STRING_ARRAY = new ShortLEArrayType<>(STRING);

    public static final Type<Position3f> POSITION_3F = new Position3fType();
    public static final Type<Position2f> POSITION_2F = new Position2fType();

}
