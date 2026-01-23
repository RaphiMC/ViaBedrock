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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.nbt.tag.*;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;

import java.util.ArrayList;
import java.util.List;

public class StructureBlockBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    private static final List<String> ROTATION_MODES = new ArrayList<>();
    private static final List<String> MIRROR_MODES = new ArrayList<>();
    private static final List<String> DATA_MODES = new ArrayList<>();

    static {
        ROTATION_MODES.add("NONE");
        ROTATION_MODES.add("CLOCKWISE_90");
        ROTATION_MODES.add("CLOCKWISE_180");
        ROTATION_MODES.add("COUNTERCLOCKWISE_90");

        MIRROR_MODES.add("NONE");
        MIRROR_MODES.add("LEFT_RIGHT");
        MIRROR_MODES.add("FRONT_BACK");

        // Clientside default mapping for invalid data modes is "DATA"
        DATA_MODES.add("INVALID");
        DATA_MODES.add("SAVE");
        DATA_MODES.add("LOAD");
        DATA_MODES.add("CORNER");
        DATA_MODES.add("DATA");
        DATA_MODES.add("EXPORT");
    }

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("rotation") instanceof ByteTag rotationTag) {
            final byte rotation = rotationTag.asByte();
            if (rotation >= 0 && rotation < ROTATION_MODES.size()) {
                javaTag.putString("rotation", ROTATION_MODES.get(rotation));
            }
        }
        if (bedrockTag.get("mirror") instanceof ByteTag mirrorTag) {
            final byte mirror = mirrorTag.asByte();
            if (mirror >= 0 && mirror < MIRROR_MODES.size()) {
                javaTag.putString("mirror", MIRROR_MODES.get(mirror));
            }
        }
        if (bedrockTag.get("data") instanceof IntTag dataTag) {
            final int data = dataTag.asInt();
            if (data >= 0 && data < DATA_MODES.size()) {
                javaTag.putString("mode", DATA_MODES.get(data));
            }
        }
        if (bedrockTag.get("integrity") instanceof FloatTag integrityTag) {
            javaTag.putFloat("integrity", integrityTag.asFloat() / 100F);
        }

        this.copy(bedrockTag, javaTag, "structureName", "name", StringTag.class);
        this.copy(bedrockTag, javaTag, "xStructureOffset", "posX", IntTag.class);
        this.copy(bedrockTag, javaTag, "yStructureOffset", "posY", IntTag.class);
        this.copy(bedrockTag, javaTag, "zStructureOffset", "posZ", IntTag.class);
        this.copy(bedrockTag, javaTag, "xStructureSize", "sizeX", IntTag.class);
        this.copy(bedrockTag, javaTag, "yStructureSize", "sizeY", IntTag.class);
        this.copy(bedrockTag, javaTag, "zStructureSize", "sizeZ", IntTag.class);
        this.copy(bedrockTag, javaTag, "ignoreEntities", ByteTag.class);
        this.copy(bedrockTag, javaTag, "isPowered", "powered", ByteTag.class);
        this.copy(bedrockTag, javaTag, "removeBlocks", "showair", ByteTag.class); // Not correct, but at least it's something
        this.copy(bedrockTag, javaTag, "showBoundingBox", "showboundingbox", ByteTag.class);
        this.copy(bedrockTag, javaTag, "seed", LongTag.class);

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
