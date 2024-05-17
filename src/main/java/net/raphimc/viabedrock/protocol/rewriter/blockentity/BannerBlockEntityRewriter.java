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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.chunk.BlockEntityWithBlockState;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.DyeColor;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;

import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;

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

        if (bedrockTag.get("Type") instanceof IntTag typeTag) {
            final int type = typeTag.asInt();
            if (type == 1) { // ominous banner
                bedrockTag.putInt("Base", DyeColor.WHITE.bedrockId());
                final ListTag<CompoundTag> patterns = new ListTag<>(CompoundTag.class);
                patterns.add(this.createBedrockPattern("mr", DyeColor.CYAN));
                patterns.add(this.createBedrockPattern("bs", DyeColor.LIGHT_GRAY));
                patterns.add(this.createBedrockPattern("cs", DyeColor.GRAY));
                patterns.add(this.createBedrockPattern("bo", DyeColor.LIGHT_GRAY));
                patterns.add(this.createBedrockPattern("ms", DyeColor.BLACK));
                patterns.add(this.createBedrockPattern("hh", DyeColor.LIGHT_GRAY));
                patterns.add(this.createBedrockPattern("mc", DyeColor.LIGHT_GRAY));
                patterns.add(this.createBedrockPattern("bo", DyeColor.BLACK));
                bedrockTag.put("Patterns", patterns);
            }
        }

        final ListTag<CompoundTag> bedrockPatterns = bedrockTag.getListTag("Patterns", CompoundTag.class);
        if (bedrockPatterns != null) {
            final ListTag<CompoundTag> javaPatterns = new ListTag<>(CompoundTag.class);
            for (CompoundTag bedrockPattern : bedrockPatterns) {
                if (!(bedrockPattern.get("Pattern") instanceof StringTag patternTag)) continue;

                final String pattern = patternTag.getValue();
                final DyeColor color = DyeColor.getByBedrockId(bedrockPattern.getInt("Color", DyeColor.BLACK.bedrockId()), DyeColor.PURPLE);
                final String javaPattern = BedrockProtocol.MAPPINGS.getBedrockToJavaBannerPatterns().get(pattern);
                if (javaPattern != null) {
                    javaPatterns.add(this.createJavaPattern(javaPattern, color));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown banner pattern: " + pattern);
                }
            }
            javaTag.put("patterns", javaPatterns);
        }

        int javaBlockState = user.get(ChunkTracker.class).getJavaBlockState(bedrockBlockEntity.position());
        if (bedrockTag.get("Base") instanceof IntTag baseTag && baseTag.asInt() != DyeColor.BLACK.bedrockId()) {
            final DyeColor baseColor = DyeColor.getByBedrockId(baseTag.asInt(), DyeColor.PURPLE);
            final boolean isStandingBanner = BedrockProtocol.MAPPINGS.getJavaBlockStates().inverse().get(javaBlockState).identifier().equals("black_banner");
            javaBlockState -= baseColor.bedrockId() * (isStandingBanner ? 16 : 4);
        }

        return new BlockEntityWithBlockState(new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag), javaBlockState);
    }

    private CompoundTag createJavaPattern(final String pattern, final DyeColor color) {
        final CompoundTag patternTag = new CompoundTag();
        patternTag.putString("pattern", pattern);
        patternTag.putString("color", color.name().toLowerCase(Locale.ROOT));
        return patternTag;
    }

    private CompoundTag createBedrockPattern(final String pattern, final DyeColor color) {
        final CompoundTag patternTag = new CompoundTag();
        patternTag.putString("Pattern", pattern);
        patternTag.putInt("Color", color.bedrockId());
        return patternTag;
    }

}
