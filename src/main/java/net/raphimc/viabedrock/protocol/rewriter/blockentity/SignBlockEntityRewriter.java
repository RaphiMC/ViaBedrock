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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.nbt.tag.*;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTextUtils;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.data.enums.DyeColor;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;

import java.util.ArrayList;
import java.util.List;

public class SignBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    private static final String[] TEXT_CONTAINER_KEYS = {"FrontText", "BackText"};

    public static void upgradeData(final CompoundTag data) {
        if (data.get("Text1") instanceof StringTag) {
            final StringBuilder textBuilder = new StringBuilder();
            for (int i = 1; i <= 4; i++) {
                if (!textBuilder.isEmpty()) {
                    textBuilder.append("\n§r");
                }
                textBuilder.append(data.getString("Text" + i, ""));
            }

            final CompoundTag frontText = new CompoundTag();
            frontText.putString("Text", textBuilder.toString());
            frontText.putByte("IgnoreLighting", (byte) 0);
            frontText.putByte("PersistFormatting", (byte) 1);

            data.clear();
            data.put("FrontText", frontText);
        } else if (data.get("Text") instanceof StringTag) {
            if (!(data.get("TextIgnoreLegacyBugResolved") instanceof ByteTag textIgnoreLegacyBugResolvedTag && textIgnoreLegacyBugResolvedTag.asByte() != 0)) {
                data.putByte("HideGlowOutline", (byte) 1);
            }

            final CompoundTag frontText = data.copy();
            data.keySet().removeIf(key -> !key.equals("IsWaxed") && !key.equals("LockedForEditingBy"));
            data.put("FrontText", frontText);
        }
    }

    public static void sanitizeData(final CompoundTag data) {
        for (String textContainerKey : TEXT_CONTAINER_KEYS) {
            if (!(data.get(textContainerKey) instanceof CompoundTag)) {
                final CompoundTag textContainer = new CompoundTag();
                textContainer.putByte("IgnoreLighting", (byte) 0);
                textContainer.putByte("PersistFormatting", (byte) 1);
                data.put(textContainerKey, textContainer);
            }
            final CompoundTag textContainer = data.getCompoundTag(textContainerKey);
            final CompoundTag sanitizedTextContainer = new CompoundTag();
            if (textContainer.get("FilteredText") instanceof StringTag filteredText) {
                sanitizedTextContainer.put("FilteredText", filteredText);
            } else {
                sanitizedTextContainer.putString("FilteredText", "");
            }
            if (textContainer.get("HideGlowOutline") instanceof ByteTag hideGlowOutline) {
                sanitizedTextContainer.put("HideGlowOutline", hideGlowOutline);
            } else {
                sanitizedTextContainer.putByte("HideGlowOutline", (byte) 0);
            }
            if (textContainer.get("IgnoreLighting") instanceof ByteTag ignoreLighting) {
                sanitizedTextContainer.put("IgnoreLighting", ignoreLighting);
            } else {
                sanitizedTextContainer.putByte("IgnoreLighting", (byte) 1);
            }
            if (textContainer.get("PersistFormatting") instanceof ByteTag persistFormatting) {
                sanitizedTextContainer.put("PersistFormatting", persistFormatting);
            } else {
                sanitizedTextContainer.putByte("PersistFormatting", (byte) 0);
            }
            if (textContainer.get("SignTextColor") instanceof IntTag signTextColor) {
                sanitizedTextContainer.put("SignTextColor", signTextColor);
            } else {
                sanitizedTextContainer.putInt("SignTextColor", DyeColor.BLACK.signColor());
            }
            if (textContainer.get("Text") instanceof StringTag text) {
                sanitizedTextContainer.put("Text", text);
            } else {
                sanitizedTextContainer.putString("Text", "");
            }
            if (textContainer.get("TextOwner") instanceof StringTag textOwner) {
                sanitizedTextContainer.put("TextOwner", textOwner);
            } else {
                sanitizedTextContainer.putString("TextOwner", "");
            }
            data.put(textContainerKey, sanitizedTextContainer);
        }
        if (!(data.get("IsWaxed") instanceof ByteTag)) {
            data.putByte("IsWaxed", (byte) 0);
        }
        if (!(data.get("LockedForEditingBy") instanceof LongTag)) {
            data.putLong("LockedForEditingBy", -1L);
        }
    }

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag().copy();
        upgradeData(bedrockTag);
        final CompoundTag javaTag = new CompoundTag();

        if (bedrockTag.get("FrontText") instanceof CompoundTag frontText) {
            javaTag.put("front_text", this.translateText(frontText));
        }
        if (bedrockTag.get("BackText") instanceof CompoundTag backText) {
            javaTag.put("back_text", this.translateText(backText));
        }
        this.copy(bedrockTag, javaTag, "IsWaxed", "is_waxed", ByteTag.class);

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

    private CompoundTag translateText(final CompoundTag bedrockText) {
        final CompoundTag javaText = new CompoundTag();

        final String text = bedrockText.getString("Text", "");
        final List<TextComponent> components = new ArrayList<>();
        if (bedrockText.get("PersistFormatting") instanceof ByteTag persistFormatting && persistFormatting.asByte() != 0) {
            for (String line : BedrockTextUtils.split(text, "\n")) {
                components.add(TextUtil.stringToTextComponent(line));
            }
        } else {
            for (String line : text.split("\n")) {
                components.add(TextUtil.stringToTextComponent(line));
            }
        }

        final boolean ignoreLighting = !(bedrockText.get("IgnoreLighting") instanceof ByteTag ignoreLightingTag) || ignoreLightingTag.asByte() != 0;
        final boolean hideGlowOutline = bedrockText.get("HideGlowOutline") instanceof ByteTag hideGlowOutlineTag && hideGlowOutlineTag.asByte() != 0;
        if (!hideGlowOutline && ignoreLighting) {
            javaText.putByte("has_glowing_text", (byte) 1);
        }

        if (bedrockText.get("SignTextColor") instanceof IntTag signTextColorTag) {
            final int signTextColor = signTextColorTag.asInt();
            if (((signTextColor >> 24) & 0xFF) < 100) { // Bedrock client can't properly render very transparent text
                components.clear();
            }
            javaText.putString("color", DyeColor.getClosestDyeColor(signTextColor).name().toLowerCase());
            for (TextComponent component : components) {
                component.forEach(c -> c.getStyle().setColor(signTextColor));
            }
        }

        final ListTag<CompoundTag> messages = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < 4; i++) {
            messages.add(TextUtil.ensureCompoundTag(components.size() > i ? TextUtil.textComponentToNbt(components.get(i)) : TextUtil.stringToNbt("")));
        }
        javaText.put("messages", messages);

        return javaText;
    }

}
