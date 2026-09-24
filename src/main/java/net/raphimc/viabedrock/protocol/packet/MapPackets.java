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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.util.map.JavaMapPaletteUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ClientboundMapItemDataPacket_Type;
import net.raphimc.viabedrock.protocol.storage.MapTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;

public final class MapPackets {

    private static final int TEXTURE_UPDATE = ClientboundMapItemDataPacket_Type.TextureUpdate.getValue();
    private static final int DECORATION_UPDATE = ClientboundMapItemDataPacket_Type.DecorationUpdate.getValue();
    private static final int CREATION = ClientboundMapItemDataPacket_Type.Creation.getValue();

    private MapPackets() {
    }

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.MAP_ITEM_DATA, ClientboundPackets26_3.MAP_ITEM_DATA, wrapper -> {
            final long bedrockId = wrapper.read(BedrockTypes.VAR_LONG);
            final int flags = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
            wrapper.read(Types.BYTE); // dimension
            final boolean locked = wrapper.read(Types.BOOLEAN);
            wrapper.read(BedrockTypes.BLOCK_POSITION); // origin

            if ((flags & CREATION) != 0) {
                final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
                for (int i = 0; i < count; i++) {
                    wrapper.read(BedrockTypes.VAR_LONG); // related map id
                }
            }

            final byte scale = (flags & (CREATION | DECORATION_UPDATE | TEXTURE_UPDATE)) != 0
                    ? wrapper.read(Types.BYTE) : 0;
            final List<Decoration> decorations = new ArrayList<>();
            if ((flags & DECORATION_UPDATE) != 0) {
                final int trackedCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
                for (int i = 0; i < trackedCount; i++) {
                    switch (wrapper.read(BedrockTypes.INT_LE)) {
                        case 0 -> wrapper.read(BedrockTypes.VAR_LONG); // entity id
                        case 1 -> wrapper.read(BedrockTypes.BLOCK_POSITION); // block position
                        case 2 -> { // Other has no payload.
                        }
                        default -> throw new IllegalArgumentException("Unknown map tracked actor type");
                    }
                }

                final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
                for (int i = 0; i < count; i++) {
                    final int type = Byte.toUnsignedInt(wrapper.read(Types.BYTE));
                    final byte rotation = wrapper.read(Types.BYTE);
                    final byte x = wrapper.read(Types.BYTE);
                    final byte y = wrapper.read(Types.BYTE);
                    final String label = wrapper.read(BedrockTypes.STRING);
                    wrapper.read(BedrockTypes.INT_LE); // color, used by Bedrock markers
                    final int javaType = javaDecorationType(type);
                    if (javaType != -1) {
                        decorations.add(new Decoration(javaType, x, y, rotation, label));
                    }
                }
            }

            int width = 0;
            int height = 0;
            int startX = 0;
            int startY = 0;
            short[] colors = new short[0];
            if ((flags & TEXTURE_UPDATE) != 0) {
                width = wrapper.read(BedrockTypes.VAR_INT);
                height = wrapper.read(BedrockTypes.VAR_INT);
                startX = wrapper.read(BedrockTypes.VAR_INT);
                startY = wrapper.read(BedrockTypes.VAR_INT);
                final int colorCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT);
                if (width < 0 || height < 0 || startX < 0 || startY < 0
                        || width + startX > 128 || height + startY > 128 || colorCount != width * height) {
                    wrapper.cancel();
                    return;
                }

                final int[] bedrockColors = new int[colorCount];
                for (int i = 0; i < colorCount; i++) {
                    bedrockColors[i] = (int) (long) wrapper.read(BedrockTypes.UNSIGNED_INT_LE);
                }
                colors = JavaMapPaletteUtil.convertToJavaPalette(bedrockColors);
            }

            wrapper.write(Types.VAR_INT, wrapper.user().get(MapTracker.class).javaId(bedrockId));
            wrapper.write(Types.BYTE, scale);
            wrapper.write(Types.BOOLEAN, locked);
            wrapper.write(Types.BOOLEAN, (flags & DECORATION_UPDATE) != 0);
            if ((flags & DECORATION_UPDATE) != 0) {
                wrapper.write(Types.VAR_INT, decorations.size());
                for (Decoration decoration : decorations) {
                    wrapper.write(Types.VAR_INT, decoration.type());
                    wrapper.write(Types.BYTE, decoration.x());
                    wrapper.write(Types.BYTE, decoration.y());
                    wrapper.write(Types.BYTE, decoration.rotation());
                    wrapper.write(Types.OPTIONAL_TAG, decoration.label().isEmpty() ? null : TextUtil.stringToNbt(decoration.label()));
                }
            }
            wrapper.write(Types.UNSIGNED_BYTE, (short) width);
            if (width > 0) {
                wrapper.write(Types.UNSIGNED_BYTE, (short) height);
                wrapper.write(Types.UNSIGNED_BYTE, (short) startX);
                wrapper.write(Types.UNSIGNED_BYTE, (short) startY);
                wrapper.write(Types.VAR_INT, colors.length);
                for (short color : colors) {
                    wrapper.write(Types.UNSIGNED_BYTE, color);
                }
            }
        });
    }

    private static int javaDecorationType(final int bedrockType) {
        return switch (bedrockType) {
            case 0 -> 0; // player
            case 1 -> 23; // green banner
            case 2 -> 24; // red banner
            case 3 -> 21; // blue banner
            case 4 -> 4; // target x
            case 5 -> 2; // red marker
            case 6 -> 5; // target point
            case 7 -> 10; // white banner
            case 8 -> 16; // pink banner
            case 9 -> 11; // orange banner
            case 10 -> 14; // yellow banner
            case 11 -> 19; // cyan banner
            case 12 -> 23; // green banner
            case 13 -> 1; // frame
            case 14 -> 8; // mansion
            case 15 -> 9; // monument
            case 17, 18, 19, 20, 21, 22, 23, 24 -> bedrockType + 10; // structures
            default -> -1;
        };
    }

    private record Decoration(int type, byte x, byte y, byte rotation, String label) {
    }
}
