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

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import io.netty.util.AsciiString;
import net.raphimc.viabedrock.protocol.model.*;

import java.util.UUID;

public class BedrockTypes {

    public static final ShortLEType SHORT_LE = new ShortLEType();
    public static final UnsignedShortLEType UNSIGNED_SHORT_LE = new UnsignedShortLEType();
    public static final IntLEType INT_LE = new IntLEType();
    public static final FloatLEType FLOAT_LE = new FloatLEType();
    public static final LongLEType LONG_LE = new LongLEType();

    public static final VarIntType VAR_INT = new VarIntType();
    public static final UnsignedVarIntType UNSIGNED_VAR_INT = new UnsignedVarIntType();
    public static final VarLongType VAR_LONG = new VarLongType();
    public static final UnsignedVarLongType UNSIGNED_VAR_LONG = new UnsignedVarLongType();
    public static final Type<Long[]> LONG_ARRAY = new ArrayType<>(LONG_LE, UNSIGNED_VAR_INT);
    public static final Type<byte[]> BYTE_ARRAY = new ByteArrayType();
    public static final Type<AsciiString> ASCII_STRING = new AsciiStringType();
    public static final Type<String> STRING = new StringType();
    public static final Type<String[]> SHORT_LE_STRING_ARRAY = new ArrayType<>(STRING, SHORT_LE);
    public static final Type<String[]> UNSIGNED_VAR_INT_STRING_ARRAY = new ArrayType<>(STRING, UNSIGNED_VAR_INT);
    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(UUID, UNSIGNED_VAR_INT);

    public static final Type<Tag> TAG = new TagType();
    public static final Type<Position> POSITION_3I = new Position3iType();
    public static final Type<Position3f> POSITION_3F = new Position3fType();
    public static final Type<Position2f> POSITION_2F = new Position2fType();
    public static final Type<GameRule<?>> GAME_RULE = new GameRuleType();
    public static final Type<GameRule<?>[]> GAME_RULE_ARRAY = new ArrayType<>(GAME_RULE, UNSIGNED_VAR_INT);
    public static final Type<Experiment> EXPERIMENT = new ExperimentType();
    public static final Type<Experiment[]> EXPERIMENT_ARRAY = new ArrayType<>(EXPERIMENT, INT_LE);
    public static final Type<EducationUriResource> EDUCATION_URI_RESOURCE = new EducationUriResourceType();
    public static final Type<BlockProperties> BLOCK_PROPERTIES = new BlockPropertiesType();
    public static final Type<BlockProperties[]> BLOCK_PROPERTIES_ARRAY = new ArrayType<>(BLOCK_PROPERTIES, UNSIGNED_VAR_INT);
    public static final Type<ItemEntry> ITEM_ENTRY = new ItemEntryType();
    public static final Type<ItemEntry[]> ITEM_ENTRY_ARRAY = new ArrayType<>(ITEM_ENTRY, UNSIGNED_VAR_INT);
    public static final Type<CommandOrigin> COMMAND_ORIGIN = new CommandOriginType();

}
