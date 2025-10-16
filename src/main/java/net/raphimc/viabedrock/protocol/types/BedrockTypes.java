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
package net.raphimc.viabedrock.protocol.types;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.Triple;
import net.raphimc.viabedrock.api.chunk.datapalette.BedrockDataPalette;
import net.raphimc.viabedrock.api.chunk.section.BedrockChunkSection;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.experimental.model.inventory.BedrockInventoryTransaction;
import net.raphimc.viabedrock.experimental.model.inventory.InventoryActionData;
import net.raphimc.viabedrock.experimental.model.inventory.InventorySource;
import net.raphimc.viabedrock.experimental.model.inventory.LegacySetItemSlotData;
import net.raphimc.viabedrock.experimental.types.inventory.BedrockInventoryTransactionType;
import net.raphimc.viabedrock.experimental.types.inventory.InventoryActionDataType;
import net.raphimc.viabedrock.experimental.types.inventory.InventorySourcePacketType;
import net.raphimc.viabedrock.experimental.types.inventory.LegacySetItemSlotDataType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.Tag_Type;
import net.raphimc.viabedrock.protocol.model.*;
import net.raphimc.viabedrock.protocol.types.array.ArrayType;
import net.raphimc.viabedrock.protocol.types.array.ByteArrayType;
import net.raphimc.viabedrock.protocol.types.chunk.ChunkSectionType;
import net.raphimc.viabedrock.protocol.types.chunk.DataPaletteType;
import net.raphimc.viabedrock.protocol.types.entitydata.EntityDataType;
import net.raphimc.viabedrock.protocol.types.entitydata.EntityPropertiesType;
import net.raphimc.viabedrock.protocol.types.model.*;
import net.raphimc.viabedrock.protocol.types.position.*;
import net.raphimc.viabedrock.protocol.types.primitive.*;

import java.awt.image.BufferedImage;
import java.math.BigInteger;
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
    public static final Type<BigInteger> UNSIGNED_VAR_BIG_INTEGER = new UnsignedVarBigIntegerType();
    public static final Type<Long[]> LONG_ARRAY = new ArrayType<>(LONG_LE, UNSIGNED_VAR_INT);
    public static final Type<byte[]> BYTE_ARRAY = new ByteArrayType();
    public static final Type<String> ASCII_STRING = new AsciiStringType();
    public static final Type<String> STRING = new StringType();
    public static final Type<String[]> SHORT_LE_STRING_ARRAY = new ArrayType<>(STRING, SHORT_LE);
    public static final Type<String[]> STRING_ARRAY = new ArrayType<>(STRING, UNSIGNED_VAR_INT);
    public static final Type<String> UTF8_STRING = new Utf8StringType();
    public static final Type<String[]> UTF8_STRING_ARRAY = new ArrayType<>(UTF8_STRING, UNSIGNED_INT_LE);
    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(UUID, UNSIGNED_VAR_INT);
    public static final Type<BufferedImage> IMAGE = new ImageType();

    public static final Type<Tag> NETWORK_TAG = new TagType();
    public static final Type<Tag> TAG_LE = new TagLEType();
    public static final Type<Tag> COMPOUND_TAG_VALUE = new TagValueType(Tag_Type.Compound);
    public static final Type<BlockPosition> BLOCK_POSITION = new BlockPositionType();
    public static final Type<BlockPosition> POSITION_3I = new Position3iType();
    public static final Type<Position3f> POSITION_3F = new Position3fType();
    public static final Type<Position2f> POSITION_2F = new Position2fType();
    public static final Type<GameRule> GAME_RULE = new GameRuleType(false);
    public static final Type<GameRule[]> GAME_RULE_ARRAY = new ArrayType<>(GAME_RULE, UNSIGNED_VAR_INT);
    public static final Type<GameRule> VAR_INT_GAME_RULE = new GameRuleType(true);
    public static final Type<GameRule[]> VAR_INT_GAME_RULE_ARRAY = new ArrayType<>(VAR_INT_GAME_RULE, UNSIGNED_VAR_INT);
    public static final Type<Experiment> EXPERIMENT = new ExperimentType();
    public static final Type<Experiment[]> EXPERIMENT_ARRAY = new ArrayType<>(EXPERIMENT, UNSIGNED_INT_LE);
    public static final Type<EducationUriResource> EDUCATION_URI_RESOURCE = new EducationUriResourceType();
    public static final Type<BlockProperties> BLOCK_PROPERTIES = new BlockPropertiesType();
    public static final Type<BlockProperties[]> BLOCK_PROPERTIES_ARRAY = new ArrayType<>(BLOCK_PROPERTIES, UNSIGNED_VAR_INT);
    public static final Type<ItemEntry> ITEM_ENTRY = new ItemEntryType();
    public static final Type<ItemEntry[]> ITEM_ENTRY_ARRAY = new ArrayType<>(ITEM_ENTRY, UNSIGNED_VAR_INT);
    public static final Type<CommandOriginData> COMMAND_ORIGIN_DATA = new CommandOriginDataType();
    public static final Type<BedrockChunkSection> CHUNK_SECTION = new ChunkSectionType();
    public static final Type<BlockPosition> SUB_CHUNK_OFFSET = new SubChunkOffsetType();
    public static final Type<ResourcePack> RESOURCE_PACK = new ResourcePackType();
    public static final Type<ResourcePack[]> RESOURCE_PACK_ARRAY = new ArrayType<>(RESOURCE_PACK, UNSIGNED_SHORT_LE);
    public static final Type<Pair<UUID, String>> PACK_ID_AND_VERSION = new PackIdAndVersionType();
    public static final Type<Triple<UUID, String, String>> PACK_ID_AND_VERSION_AND_NAME = new PackIdAndVersionAndNameType();
    public static final Type<Triple<UUID, String, String>[]> PACK_ID_AND_VERSION_AND_NAME_ARRAY = new ArrayType<>(PACK_ID_AND_VERSION_AND_NAME, UNSIGNED_VAR_INT);
    public static final Type<BlockChangeEntry> BLOCK_CHANGE_ENTRY = new BlockChangeEntryType();
    public static final Type<BlockChangeEntry[]> BLOCK_CHANGE_ENTRY_ARRAY = new ArrayType<>(BLOCK_CHANGE_ENTRY, UNSIGNED_VAR_INT);
    public static final Type<BedrockDataPalette> DATA_PALETTE = new DataPaletteType(true);
    public static final Type<BedrockDataPalette> RUNTIME_DATA_PALETTE = new DataPaletteType(false);
    public static final Type<EntityData> ENTITY_DATA = new EntityDataType();
    public static final Type<EntityData[]> ENTITY_DATA_ARRAY = new ArrayType<>(ENTITY_DATA, UNSIGNED_VAR_INT);
    public static final Type<EntityProperties> ENTITY_PROPERTIES = new EntityPropertiesType();
    public static final Type<EntityLink> ENTITY_LINK = new EntityLinkType();
    public static final Type<EntityLink[]> ENTITY_LINK_ARRAY = new ArrayType<>(ENTITY_LINK, UNSIGNED_VAR_INT);
    public static final Type<SkinData> SKIN = new SkinType();
    public static final Type<PlayerAbilities> PLAYER_ABILITIES = new PlayerAbilitiesType();
    public static final Type<CommandData[]> COMMAND_DATA_ARRAY = new CommandDataArrayType();
    public static final Type<FullContainerName> FULL_CONTAINER_NAME = new FullContainerNameType();
    public static final Type<FullContainerName[]> FULL_CONTAINER_NAME_ARRAY = new ArrayType<>(FULL_CONTAINER_NAME, UNSIGNED_VAR_INT);

    //TODO: Refactor to Experimental
    public static final Type<BedrockInventoryTransaction> INVENTORY_TRANSACTION = new BedrockInventoryTransactionType();
    public static final Type<LegacySetItemSlotData[]> LEGACY_SET_ITEM_SLOT_DATA = new ArrayType<>(new LegacySetItemSlotDataType(), UNSIGNED_VAR_INT);
    public static final Type<InventorySource> INVENTORY_SOURCE = new InventorySourcePacketType();
    public static final Type<InventoryActionData[]> INVENTORY_ACTION_DATA = new ArrayType<>(new InventoryActionDataType(), UNSIGNED_VAR_INT);

}
