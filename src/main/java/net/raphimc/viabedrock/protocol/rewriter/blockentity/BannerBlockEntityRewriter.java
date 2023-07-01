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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.DyeColor;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

import java.util.Collections;

public class BannerBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    static {
        final BlockState blockState = new BlockState("black_banner", Collections.singletonMap("rotation", "0"));
        if (!BedrockProtocol.MAPPINGS.getJavaBlockStates().containsKey(blockState)) {
            throw new IllegalStateException("Unable to find black banner block state with rotation 0");
        }
    }

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("Type") instanceof IntTag) {
            final int type = bedrockTag.<IntTag>get("Type").asInt();
            if (type == 1) { // ominous banner
                bedrockTag.put("Base", new IntTag(DyeColor.WHITE.bedrockId()));
                final ListTag patterns = new ListTag();
                patterns.add(this.createPattern("mr", 6));
                patterns.add(this.createPattern("bs", 7));
                patterns.add(this.createPattern("cs", 8));
                patterns.add(this.createPattern("bo", 7));
                patterns.add(this.createPattern("ms", 0));
                patterns.add(this.createPattern("hh", 7));
                patterns.add(this.createPattern("mc", 7));
                patterns.add(this.createPattern("bo", 0));
                bedrockTag.put("Patterns", patterns);
            }
        }

        if (bedrockTag.get("Patterns") instanceof ListTag) {
            final ListTag bedrockPatterns = bedrockTag.get("Patterns");
            if (CompoundTag.class.equals(bedrockPatterns.getElementType())) {
                final ListTag javaPatterns = new ListTag();
                for (Tag bedrockPatternTag : bedrockPatterns) {
                    final CompoundTag bedrockPattern = (CompoundTag) bedrockPatternTag;
                    if (!(bedrockPattern.get("Pattern") instanceof StringTag)) continue;

                    final String pattern = bedrockPattern.<StringTag>get("Pattern").getValue();
                    final DyeColor color = DyeColor.getByBedrockId(bedrockPattern.get("Color") instanceof IntTag ? bedrockPattern.<IntTag>get("Color").asInt() : DyeColor.BLACK.bedrockId(), DyeColor.PURPLE);
                    javaPatterns.add(this.createPattern(pattern, color.javaId()));
                }
                javaTag.put("Patterns", javaPatterns);
            }
        }

        int javaBlockState = user.get(ChunkTracker.class).getJavaBlockState(bedrockBlockEntity.position());
        if (bedrockTag.get("Base") instanceof IntTag && bedrockTag.<IntTag>get("Base").asInt() != DyeColor.BLACK.bedrockId()) {
            final DyeColor baseColor = DyeColor.getByBedrockId(bedrockTag.<IntTag>get("Base").asInt(), DyeColor.PURPLE);
            final boolean isStandingBanner = BedrockProtocol.MAPPINGS.getJavaBlockStates().inverse().get(javaBlockState).identifier().equals("black_banner");
            javaBlockState -= baseColor.bedrockId() * (isStandingBanner ? 16 : 4);
        }

        return new BlockEntityWithBlockState(new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag), javaBlockState);
    }

    private CompoundTag createPattern(final String pattern, final int color) {
        final CompoundTag patternTag = new CompoundTag();
        patternTag.put("Pattern", new StringTag(pattern));
        patternTag.put("Color", new IntTag(color));
        return patternTag;
    }

}
