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
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTextUtils;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.data.enums.DyeColor;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;

import java.util.ArrayList;
import java.util.List;

public class SignBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("Text") instanceof StringTag) {
            final boolean textIgnoreLegacyBugResolved = bedrockTag.get("TextIgnoreLegacyBugResolved") instanceof ByteTag && bedrockTag.<ByteTag>get("TextIgnoreLegacyBugResolved").asByte() != 0;
            bedrockTag.put("HideGlowOutline", new ByteTag(textIgnoreLegacyBugResolved ? (byte) 0 : (byte) 1));

            javaTag.put("front_text", this.translateText(bedrockTag));

        } else {
            if (bedrockTag.get("FrontText") instanceof CompoundTag) {
                javaTag.put("front_text", this.translateText(bedrockTag.get("FrontText")));
            }
            if (bedrockTag.get("BackText") instanceof CompoundTag) {
                javaTag.put("back_text", this.translateText(bedrockTag.get("BackText")));
            }
        }

        this.copy(bedrockTag, javaTag, "IsWaxed", "is_waxed", ByteTag.class);

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

    private CompoundTag translateText(final CompoundTag bedrockText) {
        final CompoundTag javaText = new CompoundTag();

        StringBuilder textBuilder = new StringBuilder();
        if (bedrockText.get("Text") instanceof StringTag) {
            textBuilder = new StringBuilder(bedrockText.<StringTag>get("Text").getValue());
        } else {
            for (int i = 1; i <= 4; i++) {
                final String key = "Text" + i;
                if (bedrockText.get(key) instanceof StringTag) {
                    final String text = bedrockText.<StringTag>get(key).getValue();
                    if (text.isEmpty()) continue;

                    if (textBuilder.length() > 0) textBuilder.append('\n');
                    textBuilder.append(text);
                }
            }
        }
        final String text = textBuilder.toString();

        final List<ATextComponent> components = new ArrayList<>();
        if (bedrockText.get("PersistFormatting") instanceof ByteTag && bedrockText.<ByteTag>get("PersistFormatting").asByte() != 0) {
            for (String line : BedrockTextUtils.split(text, "\n")) {
                components.add(TextUtil.stringToComponent(line));
            }
        } else {
            for (String line : text.split("\n")) {
                components.add(TextUtil.stringToComponent(line));
            }
        }

        final boolean ignoreLighting = !(bedrockText.get("IgnoreLighting") instanceof ByteTag) || bedrockText.<ByteTag>get("IgnoreLighting").asByte() != 0;
        final boolean hideGlowOutline = bedrockText.get("HideGlowOutline") instanceof ByteTag && bedrockText.<ByteTag>get("HideGlowOutline").asByte() != 0;
        if (!hideGlowOutline && ignoreLighting) {
            javaText.put("has_glowing_text", new ByteTag((byte) 1));
        }

        if (bedrockText.get("SignTextColor") instanceof IntTag) {
            final int signTextColor = bedrockText.<IntTag>get("SignTextColor").asInt();
            if (((signTextColor >> 24) & 0xFF) < 100) { // Mojang client can't properly render very transparent text
                components.clear();
            }
            final DyeColor dyeColor = DyeColor.getClosestDyeColor(signTextColor);
            javaText.put("color", new StringTag(dyeColor.name().toLowerCase()));

            for (ATextComponent component : components) {
                component.forEach(c -> c.getStyle().setColor(signTextColor));
            }
        } else {
            javaText.put("color", new StringTag(DyeColor.LIGHT_GRAY.name().toLowerCase()));
        }

        if (!components.isEmpty()) {
            final ListTag messages = new ListTag(StringTag.class);
            for (int i = 0; i < 4; i++) {
                messages.add(new StringTag(components.size() > i ? TextUtil.componentToJson(components.get(i)) : TextUtil.stringToJson("")));
            }
            javaText.put("messages", messages);
        }

        return javaText;
    }

}
