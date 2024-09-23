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
package net.raphimc.viabedrock.api.util;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.LegacyStringDeserializer;
import com.viaversion.viaversion.libs.mcstructs.text.utils.TextUtils;
import net.lenni0451.mcstructs_bedrock.text.BedrockTextFormatting;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class TextUtil {

    private static final Function<Character, TextFormatting> BEDROCK_FORMATTING_RESOLVER = c -> Optional.ofNullable(BedrockTextFormatting.getByCode(c)).map(f -> {
        if (f.isColor()) {
            return new TextFormatting(f.getRgbValue());
        } else if (f.equals(BedrockTextFormatting.OBFUSCATED)) {
            return TextFormatting.OBFUSCATED;
        } else if (f.equals(BedrockTextFormatting.BOLD)) {
            return TextFormatting.BOLD;
        } else if (f.equals(BedrockTextFormatting.ITALIC)) {
            return TextFormatting.ITALIC;
        } else if (f.equals(BedrockTextFormatting.RESET)) {
            return TextFormatting.RESET;
        } else {
            throw new IllegalArgumentException("Unhandled BedrockTextFormatting: " + f);
        }
    }).orElse(null);

    public static String stringToJson(final String text) {
        return textComponentToJson(stringToTextComponent(text));
    }

    public static String textComponentToJson(final ATextComponent textComponent) {
        return ProtocolConstants.JAVA_TEXT_COMPONENT_SERIALIZER.serializeJsonString(textComponent);
    }

    public static JsonElement stringToGson(final String text) {
        return textComponentToGson(stringToTextComponent(text));
    }

    public static JsonElement textComponentToGson(final ATextComponent textComponent) {
        return ProtocolConstants.JAVA_TEXT_COMPONENT_SERIALIZER.serializeJsonTree(textComponent);
    }

    public static Tag stringToNbt(final String text) {
        return textComponentToNbt(stringToTextComponent(text));
    }

    public static Tag textComponentToNbt(final ATextComponent textComponent) {
        return ProtocolConstants.JAVA_TEXT_COMPONENT_SERIALIZER.serializeNbt(textComponent);
    }

    public static ATextComponent stringToTextComponent(final String text) {
        final ATextComponent textComponent = LegacyStringDeserializer.parse(appendFormattingCodesAfterColorCode(text), TextFormatting.COLOR_CHAR, ResetTrackingStyle::new, BEDROCK_FORMATTING_RESOLVER);
        final AtomicBoolean wasReset = new AtomicBoolean(false);
        TextUtils.iterateAll(textComponent, c -> {
            final Style style = c.getStyle();
            if (style instanceof ResetTrackingStyle resetTrackingStyle && resetTrackingStyle.wasReset) {
                wasReset.set(true);
            }
            if (wasReset.get()) {
                if (!style.isBold()) {
                    style.setBold(false);
                }
                if (!style.isUnderlined()) {
                    style.setUnderlined(false);
                }
                if (!style.isItalic()) {
                    style.setItalic(false);
                }
            }
        });
        return textComponent;
    }

    /**
     * Preprocesses bedrock text to fix the java client resetting the formatting after a color code
     */
    private static String appendFormattingCodesAfterColorCode(final String s) {
        final char[] chars = s.toCharArray();
        final Set<BedrockTextFormatting> styles = EnumSet.noneOf(BedrockTextFormatting.class);
        final StringBuilder out = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            out.append(c);
            if (c == BedrockTextFormatting.COLOR_CHAR) {
                if (i + 1 < chars.length) {
                    final char code = chars[++i];
                    out.append(code);
                    final BedrockTextFormatting formatting = BedrockTextFormatting.getByCode(code);
                    if (formatting == null) continue;

                    if (BedrockTextFormatting.RESET.equals(formatting)) {
                        styles.clear();
                    } else if (formatting.isColor()) {
                        for (BedrockTextFormatting style : styles) {
                            out.append(style.asString());
                        }
                    } else {
                        styles.add(formatting);
                    }
                }
            }
        }
        return out.toString();
    }

    private static final class ResetTrackingStyle extends Style {

        private boolean wasReset;

        @Override
        public Style setFormatting(final TextFormatting formatting) {
            if (formatting == TextFormatting.RESET) {
                this.wasReset = true;
            }
            return super.setFormatting(formatting);
        }

        @Override
        public Style copy() {
            final ResetTrackingStyle copy = new ResetTrackingStyle();
            copy.setParent(super.copy());
            copy.applyParent();
            copy.wasReset = this.wasReset;
            return copy;
        }

    }

}
