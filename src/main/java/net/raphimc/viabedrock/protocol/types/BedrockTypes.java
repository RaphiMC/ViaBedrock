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
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.Triple;
import io.netty.util.AsciiString;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.protocol.model.*;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;
import net.raphimc.viabedrock.protocol.types.array.ByteArrayType;
import net.raphimc.viabedrock.protocol.types.chunk.ChunkSectionType;
import net.raphimc.viabedrock.protocol.types.chunk.DataPaletteType;
import net.raphimc.viabedrock.protocol.types.metadata.FloatPropertiesType;
import net.raphimc.viabedrock.protocol.types.metadata.IntPropertiesType;
import net.raphimc.viabedrock.protocol.types.metadata.MetadataType;
import net.raphimc.viabedrock.protocol.types.model.*;
import net.raphimc.viabedrock.protocol.types.position.*;
import net.raphimc.viabedrock.protocol.types.primitive.*;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

public class BedrockTypes {

    public static final ShortLEType SHORT_LE = new ShortLEType();
    public static final UnsignedShortLEType UNSIGNED_SHORT_LE = new UnsignedShortLEType();
    public static final IntLEType INT_LE = new IntLEType();
    public static final UnsignedIntLEType UNSIGNED_INT_LE = new UnsignedIntLEType();
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
    public static final Type<String[]> STRING_ARRAY = new ArrayType<>(STRING, UNSIGNED_VAR_INT);
    public static final Type<String> UTF8_STRING = new Utf8StringType();
    public static final Type<String[]> UTF8_STRING_ARRAY = new ArrayType<>(UTF8_STRING, INT_LE);
    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(UUID, UNSIGNED_VAR_INT);
    public static final Type<BufferedImage> IMAGE = new ImageType();

    public static final Type<Tag> NETWORK_TAG = new TagType();
    public static final Type<Tag> TAG_LE = new TagLEType();
    public static final Type<Position> BLOCK_POSITION = new BlockPositionType();
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
    public static final Type<BedrockChunkSection> CHUNK_SECTION = new ChunkSectionType();
    public static final Type<Position> SUB_CHUNK_OFFSET = new SubChunkOffsetType();
    public static final Type<ResourcePack> BEHAVIOUR_PACK = new BehaviourPackType();
    public static final Type<ResourcePack[]> BEHAVIOUR_PACK_ARRAY = new ArrayType<>(BEHAVIOUR_PACK, UNSIGNED_SHORT_LE);
    public static final Type<ResourcePack> RESOURCE_PACK = new ResourcePackType();
    public static final Type<ResourcePack[]> RESOURCE_PACK_ARRAY = new ArrayType<>(RESOURCE_PACK, UNSIGNED_SHORT_LE);
    public static final Type<Pair<UUID, String>> PACK_ID_AND_VERSION = new PackIdAndVersionType();
    public static final Type<Triple<UUID, String, String>> PACK_ID_AND_VERSION_AND_NAME = new PackIdAndVersionAndNameType();
    public static final Type<Triple<UUID, String, String>[]> PACK_ID_AND_VERSION_AND_NAME_ARRAY = new ArrayType<>(PACK_ID_AND_VERSION_AND_NAME, UNSIGNED_VAR_INT);
    public static final Type<BlockChangeEntry> BLOCK_CHANGE_ENTRY = new BlockChangeEntryType();
    public static final Type<BlockChangeEntry[]> BLOCK_CHANGE_ENTRY_ARRAY = new ArrayType<>(BLOCK_CHANGE_ENTRY, UNSIGNED_VAR_INT);
    public static final Type<BedrockDataPalette> DATA_PALETTE = new DataPaletteType();
    public static final Type<Metadata> METADATA = new MetadataType();
    public static final Type<Metadata[]> METADATA_ARRAY = new ArrayType<>(METADATA, UNSIGNED_VAR_INT);
    public static final Type<SkinData> SKIN = new SkinType();
    public static final Type<Int2IntMap> INT_PROPERTIES = new IntPropertiesType();
    public static final Type<Map<Integer, Float>> FLOAT_PROPERTIES = new FloatPropertiesType();
    public static final Type<PlayerAbilities> PLAYER_ABILITIES = new PlayerAbilitiesType();
    public static final Type<EntityLink> ENTITY_LINK = new EntityLinkType();
    public static final Type<EntityLink[]> ENTITY_LINK_ARRAY = new ArrayType<>(ENTITY_LINK, UNSIGNED_VAR_INT);

}
