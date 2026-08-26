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
package net.raphimc.viabedrock.api.model.container;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

public class ChestContainer extends MenuContainer {

    public static final int SINGLE_CHEST_SIZE = 27;
    public static final int DOUBLE_CHEST_SIZE = 54;

    private ContainerEnumName containerName;

    /**
     * Creates a chest container which is backed by an entity instead of a block, so it has no block to validate.
     */
    public ChestContainer(final UserConnection user, final byte containerId, final ContainerType type, final TextComponent title, final int size) {
        super(user, containerId, type, title, null, size);
        this.containerName = ContainerEnumName.LevelEntityContainer;
    }

    public ChestContainer(final UserConnection user, final byte containerId, final TextComponent title, final BlockPosition position, final int size) {
        super(user, containerId, ContainerType.CONTAINER, title, position, size, CustomBlockTags.CHEST, CustomBlockTags.TRAPPED_CHEST, CustomBlockTags.ENDER_CHEST, CustomBlockTags.BARREL, CustomBlockTags.SHULKER_BOX);
    }

    /**
     * Bedrock stores the chest pairing in the block entity instead of the block state, so the paired chest has to be
     * looked up manually to know whether a single or a double chest menu has to be opened.
     *
     * @param user     The user connection
     * @param position The position of the chest
     * @return The position of the paired chest or null if the chest is not paired
     */
    public static BlockPosition getPairedChestPosition(final UserConnection user, final BlockPosition position) {
        final BedrockBlockEntity blockEntity = user.get(ChunkTracker.class).getBlockEntity(position);
        if (blockEntity == null) return null;
        return getPairedChestPosition(blockEntity);
    }

    public static BlockPosition getPairedChestPosition(final BedrockBlockEntity blockEntity) {
        final CompoundTag tag = blockEntity.tag();
        if (!(tag.get("pairx") instanceof NumberTag pairX) || !(tag.get("pairz") instanceof NumberTag pairZ)) {
            return null;
        }
        final BlockPosition position = blockEntity.position();
        final BlockPosition pairedPosition = new BlockPosition(pairX.asInt(), position.y(), pairZ.asInt());
        if (pairedPosition.equals(position)) { // Malformed block entity data
            return null;
        }
        return pairedPosition;
    }

    @Override
    public ContainerEnumName bedrockContainerName(final int slot) {
        if (this.containerName == null) {
            // Barrels use a dedicated container name, every other chest like container is addressed as a level entity.
            // The block can't change while the container is open, so this only has to be resolved once.
            final String tag = this.position != null ? this.user.get(BlockStateRewriter.class).tag(this.user.get(ChunkTracker.class).getBlockState(this.position)) : null;
            this.containerName = CustomBlockTags.BARREL.equals(tag) ? ContainerEnumName.BarrelContainer : ContainerEnumName.LevelEntityContainer;
        }
        return this.containerName;
    }

    @Override
    public int javaMenuType() {
        if (this.size() > SINGLE_CHEST_SIZE) {
            return BedrockProtocol.MAPPINGS.getJavaMenus().get("minecraft:generic_9x6");
        } else {
            return super.javaMenuType();
        }
    }

}
