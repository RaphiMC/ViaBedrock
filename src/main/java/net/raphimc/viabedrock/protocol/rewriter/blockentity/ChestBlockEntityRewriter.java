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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.container.ChestContainer;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

import java.util.List;

/**
 * Bedrock stores the chest pairing in the block entity ("pairx"/"pairz"), while Java stores it in the block state
 * ("type"). Without translating it, double chests are rendered as two separate single chests on the Java client.
 */
public class ChestBlockEntityRewriter extends LootableContainerBlockEntityRewriter {

    private static final List<String> HORIZONTAL_DIRECTIONS = List.of("north", "east", "south", "west");

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final BlockEntity javaBlockEntity = super.toJava(user, bedrockBlockEntity);

        final BlockPosition pairedPosition = ChestContainer.getPairedChestPosition(bedrockBlockEntity);
        if (pairedPosition == null) {
            return javaBlockEntity;
        }

        final int javaBlockStateId = user.get(ChunkTracker.class).getJavaBlockState(bedrockBlockEntity.position());
        final BlockState javaBlockState = BedrockProtocol.MAPPINGS.getJavaBlockStates().inverse().get(javaBlockStateId);
        if (javaBlockState == null || !javaBlockState.properties().containsKey("type")) {
            return javaBlockEntity;
        }
        final String facing = javaBlockState.properties().get("facing");
        if (facing == null) {
            return javaBlockEntity;
        }

        final BlockPosition position = bedrockBlockEntity.position();
        final String connectedDirection = getHorizontalDirection(pairedPosition.x() - position.x(), pairedPosition.z() - position.z());
        if (connectedDirection == null) {
            return javaBlockEntity;
        }

        // ChestBlock#getConnectedDirection: The partner of a left chest is at facing.getClockWise(), the partner of a right chest at facing.getCounterClockWise()
        final String type;
        if (connectedDirection.equals(rotate(facing, 1))) {
            type = "left";
        } else if (connectedDirection.equals(rotate(facing, -1))) {
            type = "right";
        } else { // The chests are not aligned in a way the Java client can render as a double chest
            return javaBlockEntity;
        }

        final Integer pairedJavaBlockStateId = BedrockProtocol.MAPPINGS.getJavaBlockStates().get(javaBlockState.withProperty("type", type));
        if (pairedJavaBlockStateId == null) {
            return javaBlockEntity;
        }

        return new BlockEntityWithBlockState(javaBlockEntity, pairedJavaBlockStateId);
    }

    private static String getHorizontalDirection(final int deltaX, final int deltaZ) {
        if (deltaX == 0 && deltaZ == -1) return "north";
        if (deltaX == 1 && deltaZ == 0) return "east";
        if (deltaX == 0 && deltaZ == 1) return "south";
        if (deltaX == -1 && deltaZ == 0) return "west";
        return null;
    }

    private static String rotate(final String direction, final int steps) {
        final int index = HORIZONTAL_DIRECTIONS.indexOf(direction);
        if (index == -1) return null;
        return HORIZONTAL_DIRECTIONS.get(Math.floorMod(index + steps, HORIZONTAL_DIRECTIONS.size()));
    }

}
