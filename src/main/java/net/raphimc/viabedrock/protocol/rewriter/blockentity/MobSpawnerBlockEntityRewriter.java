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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_4;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;

import java.util.logging.Level;

public class MobSpawnerBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        this.copy(bedrockTag, javaTag, "Delay", ShortTag.class);
        this.copy(bedrockTag, javaTag, "MinSpawnDelay", ShortTag.class);
        this.copy(bedrockTag, javaTag, "MaxSpawnDelay", ShortTag.class);
        this.copy(bedrockTag, javaTag, "SpawnCount", ShortTag.class);
        this.copy(bedrockTag, javaTag, "MaxNearbyEntities", ShortTag.class);
        this.copy(bedrockTag, javaTag, "RequiredPlayerRange", ShortTag.class);
        this.copy(bedrockTag, javaTag, "SpawnRange", ShortTag.class);

        if (bedrockTag.get("EntityId") instanceof IntTag entityIdTag) {
            final int bedrockEntityId = entityIdTag.asInt();
            final String bedrockEntityIdentifier = BedrockProtocol.MAPPINGS.getBedrockEntities().inverse().getOrDefault(bedrockEntityId, "viabedrock:" + bedrockEntityId);
            bedrockTag.putString("EntityIdentifier", bedrockEntityIdentifier);
        }
        if (bedrockTag.get("EntityIdentifier") instanceof StringTag entityIdentifierTag) {
            final String bedrockEntityIdentifier = entityIdentifierTag.getValue();
            final EntityTypes1_21_4 javaEntityType = BedrockProtocol.MAPPINGS.getBedrockToJavaEntities().get(Key.namespaced(bedrockEntityIdentifier));
            if (javaEntityType != null) {
                final CompoundTag spawnData = new CompoundTag();
                final CompoundTag entityTag = new CompoundTag();
                entityTag.putString("id", javaEntityType.identifier());
                spawnData.put("entity", entityTag);
                javaTag.put("SpawnData", spawnData);
            } else if (!Key.stripNamespace(bedrockEntityIdentifier).isEmpty()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock entity identifier: " + bedrockEntityIdentifier);
                final CompoundTag spawnData = new CompoundTag();
                spawnData.put("entity", new CompoundTag());
                javaTag.put("SpawnData", spawnData);
            }
        }

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
