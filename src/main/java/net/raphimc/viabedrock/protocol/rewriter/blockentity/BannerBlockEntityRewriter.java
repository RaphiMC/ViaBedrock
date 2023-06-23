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
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

public class BannerBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    private static final int COLOR_BLACK = 0;
    private static final int COLOR_PURPLE = 5;
    private static final int COLOR_WHITE = 15;

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("Type") instanceof IntTag) {
            final int type = bedrockTag.<IntTag>get("Type").asInt();
            if (type == 1) { // ominous banner
                bedrockTag.put("Base", new IntTag(COLOR_WHITE));
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
                    int color = bedrockPattern.get("Color") instanceof IntTag ? bedrockPattern.<IntTag>get("Color").asInt() : 0;
                    if (color < COLOR_BLACK || color > COLOR_WHITE) color = COLOR_PURPLE;

                    javaPatterns.add(this.createPattern(pattern, COLOR_WHITE - color));
                }
                javaTag.put("Patterns", javaPatterns);
            }
        }

        int javaBlockState = user.get(ChunkTracker.class).getJavaBlockState(bedrockBlockEntity.position());
        if (bedrockTag.get("Base") instanceof IntTag && bedrockTag.<IntTag>get("Base").asInt() != 0) {
            int base = bedrockTag.<IntTag>get("Base").asInt();
            if (base < COLOR_BLACK || base > COLOR_WHITE) base = COLOR_PURPLE;

            javaBlockState -= base * 16;
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
