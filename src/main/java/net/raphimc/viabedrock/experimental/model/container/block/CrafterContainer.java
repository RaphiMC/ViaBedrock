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
package net.raphimc.viabedrock.experimental.model.container.block;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import net.raphimc.viabedrock.experimental.model.container.ExperimentalContainer;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.data.generated.bedrock.CustomBlockTags;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

public class CrafterContainer extends ExperimentalContainer {

    private BedrockItem resultItem = BedrockItem.empty();

    public CrafterContainer(UserConnection user, byte containerId, TextComponent title, BlockPosition position) {
        super(user, containerId, ContainerType.CRAFTER, title, position, 9, CustomBlockTags.CRAFTER);

        boolean[] disabledSlots = getCrafterMetadata();
        for (short i = 0; i < 9; i++) {
            boolean disabled = disabledSlots[i];

            PacketWrapper setData = PacketWrapper.create(ClientboundPackets26_1.CONTAINER_SET_DATA, user);
            setData.write(Types.VAR_INT, (int) this.javaContainerId());
            setData.write(Types.SHORT, i);
            setData.write(Types.SHORT, (short) (disabled ? 1 : 0));
            setData.scheduleSend(BedrockProtocol.class);
        }
    }

    @Override
    public FullContainerName getFullContainerName(int slot) {
        if (slot == 50) {
            return new FullContainerName(ContainerEnumName.CreatedOutputContainer, null);
        }
        return new FullContainerName(ContainerEnumName.CrafterLevelEntityContainer, null);
    }

    @Override
    public int javaSlot(final int slot) {
        if (slot == 50) {
            return 45;
        }
        return super.javaSlot(slot);
    }

    @Override
    public int bedrockSlot(final int slot) {
        if (slot == 45) {
            return 50;
        }
        return super.bedrockSlot(slot);
    }

    @Override
    public BedrockItem getItem(final int bedrockSlot) {
        if (bedrockSlot == 50) {
            return this.resultItem;
        }
        return super.getItem(bedrockSlot);
    }

    @Override
    public boolean setItem(final int bedrockSlot, final BedrockItem item) {
        if (bedrockSlot == 50) {
            this.resultItem = item;
            return true;
        }
        return super.setItem(bedrockSlot, item);
    }

    private boolean[] getCrafterMetadata() {
        ChunkTracker ct = this.user.get(ChunkTracker.class);

        CompoundTag tag = ct.getBlockEntity(position).tag();
        if (tag == null || !tag.contains("disabled_slots")) return new boolean[9];

        boolean[] disabledSlots = new boolean[9];
        int mask = ((ShortTag) tag.get("disabled_slots")).asInt();
        for (int i = 0; i < 9; i++) {
            disabledSlots[i] = (mask & (1 << i)) != 0;
        }

        return disabledSlots;
    }

}
