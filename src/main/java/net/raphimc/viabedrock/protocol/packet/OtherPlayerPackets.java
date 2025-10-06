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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.model.entity.PlayerEntity;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.GameType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerPositionModeComponent_PositionMode;
import net.raphimc.viabedrock.protocol.data.enums.java.EquipmentSlot;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerInfoUpdateAction;
import net.raphimc.viabedrock.protocol.data.enums.java.Relative;
import net.raphimc.viabedrock.protocol.model.*;
import net.raphimc.viabedrock.protocol.provider.SkinProvider;
import net.raphimc.viabedrock.protocol.rewriter.GameTypeRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.logging.Level;

public class OtherPlayerPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.ADD_PLAYER, ClientboundPackets1_21_9.ADD_ENTITY, wrapper -> {
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final ItemRewriter itemRewriter = wrapper.user().get(ItemRewriter.class);
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final UUID uuid = wrapper.read(BedrockTypes.UUID); // uuid
            final String username = wrapper.read(BedrockTypes.STRING); // username
            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final String platformOnlineId = wrapper.read(BedrockTypes.STRING); // platform online id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f motion = wrapper.read(BedrockTypes.POSITION_3F); // motion
            final Position3f rotation = wrapper.read(BedrockTypes.POSITION_3F); // rotation
            final BedrockItem item = wrapper.read(itemRewriter.itemType()); // held item
            final GameType gameType = GameType.getByValue(wrapper.read(BedrockTypes.VAR_INT), GameType.Undefined); // game type
            final EntityData[] entityData = wrapper.read(BedrockTypes.ENTITY_DATA_ARRAY); // entity data
            final EntityProperties entityProperties = wrapper.read(BedrockTypes.ENTITY_PROPERTIES); // entity properties
            final PlayerAbilities abilities = wrapper.read(BedrockTypes.PLAYER_ABILITIES); // abilities
            final EntityLink[] entityLinks = wrapper.read(BedrockTypes.ENTITY_LINK_ARRAY); // entity links

            final PlayerEntity entity = entityTracker.addEntity(new PlayerEntity(wrapper.user(), runtimeEntityId, entityTracker.getNextJavaEntityId(), uuid, abilities));
            entity.setPosition(position);
            entity.setRotation(rotation);
            entity.updateName(username);

            final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_21_9.PLAYER_INFO_UPDATE, wrapper.user());
            playerInfoUpdate.write(Types.PROFILE_ACTIONS_ENUM1_21_4, BitSets.create(8, PlayerInfoUpdateAction.ADD_PLAYER, PlayerInfoUpdateAction.UPDATE_GAME_MODE)); // actions
            playerInfoUpdate.write(Types.VAR_INT, 1); // length
            playerInfoUpdate.write(Types.UUID, uuid); // uuid
            playerInfoUpdate.write(Types.STRING, StringUtil.encodeUUID(uuid)); // username
            playerInfoUpdate.write(Types.PROFILE_PROPERTY_ARRAY, new GameProfile.Property[]{
                    new GameProfile.Property("platform_online_id", platformOnlineId),
                    new GameProfile.Property("device_id", wrapper.read(BedrockTypes.STRING)), // device id
                    new GameProfile.Property("device_os", wrapper.read(BedrockTypes.INT_LE).toString()) // device os
            }); // properties
            playerInfoUpdate.write(Types.VAR_INT, GameTypeRewriter.getEffectiveGameMode(gameType, gameSession.getLevelGameType()).ordinal()); // game mode
            playerInfoUpdate.send(BedrockProtocol.class);

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.UUID, uuid); // uuid
            wrapper.write(Types.VAR_INT, EntityTypes1_21_9.PLAYER.getId()); // type id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.MOVEMENT_VECTOR, new Vector3d(motion.x(), motion.y(), motion.z())); // velocity
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.x())); // pitch
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.y())); // yaw
            wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.z())); // head yaw
            wrapper.write(Types.VAR_INT, 0); // data
            wrapper.send(BedrockProtocol.class);
            wrapper.cancel();

            final PacketWrapper setEquipment = PacketWrapper.create(ClientboundPackets1_21_9.SET_EQUIPMENT, wrapper.user());
            setEquipment.write(Types.VAR_INT, entity.javaId()); // entity id
            setEquipment.write(Types.BYTE, (byte) EquipmentSlot.MAINHAND.ordinal()); // slot
            setEquipment.write(VersionedTypes.V1_21_9.item, itemRewriter.javaItem(item)); // item
            setEquipment.send(BedrockProtocol.class);

            entity.updateEntityData(entityData);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.MOVE_PLAYER, ClientboundPackets1_21_9.ENTITY_POSITION_SYNC, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            final long runtimeEntityId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final Position3f rotation = wrapper.read(BedrockTypes.POSITION_3F); // rotation
            final PlayerPositionModeComponent_PositionMode mode = PlayerPositionModeComponent_PositionMode.getByValue(wrapper.read(Types.BYTE), PlayerPositionModeComponent_PositionMode.OnlyHeadRot); // mode
            final boolean onGround = wrapper.read(Types.BOOLEAN); // on ground
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // riding runtime entity id
            if (mode == PlayerPositionModeComponent_PositionMode.Teleport) {
                wrapper.read(BedrockTypes.INT_LE); // teleportation cause
                wrapper.read(BedrockTypes.INT_LE); // entity type
            }
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // tick

            final Entity entity = entityTracker.getEntityByRid(runtimeEntityId);
            if (entity == null) {
                wrapper.cancel();
                return;
            }
            if (!entity.javaType().isOrHasParent(EntityTypes1_21_9.PLAYER)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received move player packet for non-player entity: " + entity.javaType());
                wrapper.cancel();
                return;
            }
            if (mode == PlayerPositionModeComponent_PositionMode.OnlyHeadRot) {
                entity.setRotation(new Position3f(rotation.x(), entity.rotation().y(), entity.rotation().z()));
                wrapper.setPacketType(ClientboundPackets1_21_9.MOVE_ENTITY_ROT);
                wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
                wrapper.write(Types.BYTE, MathUtil.float2Byte(entity.rotation().y())); // yaw
                wrapper.write(Types.BYTE, MathUtil.float2Byte(rotation.x())); // pitch
                wrapper.write(Types.BOOLEAN, entity.isOnGround()); // on ground
                return;
            }

            entity.setPosition(position);
            entity.setRotation(rotation);
            entity.setOnGround(onGround);

            if ((mode == PlayerPositionModeComponent_PositionMode.Teleport || mode == PlayerPositionModeComponent_PositionMode.Respawn) && entity instanceof ClientPlayerEntity clientPlayer) {
                wrapper.setPacketType(ClientboundPackets1_21_9.PLAYER_POSITION);
                clientPlayer.writePlayerPositionPacketToClient(wrapper, Relative.NONE, mode == PlayerPositionModeComponent_PositionMode.Respawn);
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // entity id
            wrapper.write(Types.DOUBLE, (double) position.x()); // x
            wrapper.write(Types.DOUBLE, (double) position.y() - entity.eyeOffset()); // y
            wrapper.write(Types.DOUBLE, (double) position.z()); // z
            wrapper.write(Types.DOUBLE, 0D); // velocity x
            wrapper.write(Types.DOUBLE, 0D); // velocity y
            wrapper.write(Types.DOUBLE, 0D); // velocity z
            wrapper.write(Types.FLOAT, rotation.y()); // yaw
            wrapper.write(Types.FLOAT, rotation.x()); // pitch
            wrapper.write(Types.BOOLEAN, onGround); // on ground

            PacketFactory.sendJavaRotateHead(wrapper.user(), entity);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_SKIN, null, wrapper -> {
            wrapper.cancel();
            final UUID uuid = wrapper.read(BedrockTypes.UUID); // uuid
            final SkinData skin = wrapper.read(BedrockTypes.SKIN); // skin
            wrapper.read(BedrockTypes.STRING); // new skin name
            wrapper.read(BedrockTypes.STRING); // old skin name
            wrapper.read(Types.BOOLEAN); // trusted skin

            Via.getManager().getProviders().get(SkinProvider.class).setSkin(wrapper.user(), uuid, skin);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_ABILITIES, ClientboundPackets1_21_9.PLAYER_ABILITIES, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final PlayerAbilities abilities = wrapper.read(BedrockTypes.PLAYER_ABILITIES); // abilities
            final Entity entity = entityTracker.getEntityByUid(abilities.uniqueEntityId());
            if (entity instanceof ClientPlayerEntity clientPlayer) {
                clientPlayer.setAbilities(abilities, wrapper);
            } else if (entity instanceof PlayerEntity player) {
                player.setAbilities(abilities);
                wrapper.cancel();
            } else {
                wrapper.cancel();
            }
        });
    }

}
