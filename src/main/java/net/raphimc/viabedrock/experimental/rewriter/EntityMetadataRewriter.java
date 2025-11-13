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
package net.raphimc.viabedrock.experimental.rewriter;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorFlags;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class EntityMetadataRewriter {

    // Called in Entity#translateEntityData if experimental features are enabled
    public static boolean rewrite(final Entity entity, final ActorDataIDs id, final EntityData entityData, final List<EntityData> javaEntityData) {

        EntityData javaData = switch (id) {
            case RESERVED_0, RESERVED_092 -> { // Entity flags mask
                Set<ActorFlags> bedrockFlags = entity.entityFlags();
                byte javaBitMask = 0; // https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Entity
                if (bedrockFlags.contains(ActorFlags.ONFIRE)) {
                    javaBitMask |= (1 << 0);
                }
                if (bedrockFlags.contains(ActorFlags.SNEAKING)) {
                    javaBitMask |= (1 << 1);
                }
                if (bedrockFlags.contains(ActorFlags.RIDING)) {
                    javaBitMask |= (1 << 2);
                }
                if (bedrockFlags.contains(ActorFlags.SPRINTING)) {
                    javaBitMask |= (1 << 3);
                }
                if (bedrockFlags.contains(ActorFlags.SWIMMING)) {
                    javaBitMask |= (1 << 4);
                }
                if (bedrockFlags.contains(ActorFlags.INVISIBLE)) {
                    javaBitMask |= (1 << 5);
                }

                //TODO: Handle the other flags properly?
                //ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unhandled movement bitmask bits for entity " + entity.type() + ": " + Long.toBinaryString(remaining));

                yield new EntityData(entity.getJavaEntityDataIndex("SHARED_FLAGS"), VersionedTypes.V1_21_9.entityDataTypes().byteType, javaBitMask);
            }
            case AIR_SUPPLY -> { // Air supply is stored as a short in Bedrock, but an int in Java (Bedrock also has a max air supply value we ignore for now)
                short airSupply = (short) entityData.getValue();
                yield new EntityData(entity.getJavaEntityDataIndex("AIR_SUPPLY"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, (int) airSupply);
            }
            case POSE_INDEX -> null; // TODO: Armour stand pose index
            default -> null;
        };

        if (javaData == null) {
            // Log unhandled rewrites for debugging
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Rewriting entity data for " + id + " (" + entityData + ") -> " + entity.type());
            return false;
        } else {
            javaEntityData.add(javaData);
            return true;
        }
    }

}
