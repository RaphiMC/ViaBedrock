/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;

import java.util.Collections;
import java.util.logging.Level;

public class DecoratedPotBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("sherds") instanceof ListTag) {
            final ListTag bedrockSherds = bedrockTag.get("sherds");
            if (StringTag.class.equals(bedrockSherds.getElementType())) {
                final ListTag javaSherds = new ListTag(StringTag.class);
                for (Tag sherd : bedrockSherds) {
                    final String bedrockIdentifier = ((StringTag) sherd).getValue();
                    final ItemRewriter.Rewriter itemRewriter = BedrockProtocol.MAPPINGS.getBedrockToJavaMetaItems().getOrDefault(bedrockIdentifier, Collections.emptyMap()).getOrDefault(null, null);
                    if (itemRewriter != null) {
                        javaSherds.add(new StringTag(itemRewriter.identifier()));
                    } else if (bedrockIdentifier.isEmpty()) {
                        javaSherds.add(new StringTag("minecraft:brick"));
                    } else {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing item: " + bedrockIdentifier);
                        javaSherds.add(new StringTag("minecraft:brick"));
                    }
                }
                javaTag.put("sherds", javaSherds);
            }
        }
        if (bedrockTag.get("item") instanceof CompoundTag) {
            javaTag.put("item", this.rewriteItem(user, bedrockTag.get("item")));
        }

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
