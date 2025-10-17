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
package net.raphimc.viabedrock.experimental.util;

import com.viaversion.viaversion.api.protocol.packet.mapping.PacketMapping;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;

import java.lang.reflect.Field;

public class ProtocolUtil {

    public static void appendClientbound(final BedrockProtocol protocol, final ClientboundBedrockPackets type, final PacketHandler handler) {
        final PacketMapping mapping = protocol.getClientboundMappings().mappedPacket(type.state(), type.getId());
        if (mapping != null) {
            final PacketHandler oldHandler = mapping.handler();
            if (oldHandler == null) {
                mapping.appendHandler(handler);
            } else {
                setHandler(mapping, oldHandler.then(wrapper -> {
                    wrapper.resetReader();
                    handler.handle(wrapper);
                }));
            }
        } else {
            protocol.registerClientbound(type, null, handler);
        }
    }

    public static void appendServerbound(final BedrockProtocol protocol, final ServerboundPackets1_21_6 type, final PacketHandler handler) {
        final PacketMapping mapping = protocol.getServerboundMappings().mappedPacket(type.state(), type.getId());
        if (mapping != null) {
            final PacketHandler oldHandler = mapping.handler();
            if (oldHandler == null) {
                mapping.appendHandler(handler);
            } else {
                setHandler(mapping, oldHandler.then(wrapper -> {
                    wrapper.resetReader();
                    handler.handle(wrapper);
                }));
            }
        } else {
            protocol.registerServerbound(type, null, handler);
        }
    }

    public static void prependClientbound(final BedrockProtocol protocol, final ClientboundBedrockPackets type, final PacketHandler handler) {
        final PacketMapping mapping = protocol.getClientboundMappings().mappedPacket(type.state(), type.getId());
        if (mapping != null) {
            final PacketHandler oldHandler = mapping.handler();
            if (oldHandler == null) {
                mapping.appendHandler(handler);
            } else {
                setHandler(mapping, handler.then(wrapper -> {
                    if (!wrapper.isCancelled()) {
                        wrapper.resetReader();
                        oldHandler.handle(wrapper);
                    }
                }));
            }
        } else {
            protocol.registerClientbound(type, null, handler);
        }
    }

    public static void prependServerbound(final BedrockProtocol protocol, final ServerboundPackets1_21_6 type, final PacketHandler handler) {
        final PacketMapping mapping = protocol.getServerboundMappings().mappedPacket(type.state(), type.getId());
        if (mapping != null) {
            final PacketHandler oldHandler = mapping.handler();
            if (oldHandler == null) {
                mapping.appendHandler(handler);
            } else {
                setHandler(mapping, handler.then(wrapper -> {
                    if (!wrapper.isCancelled()) {
                        wrapper.resetReader();
                        oldHandler.handle(wrapper);
                    }
                }));
            }
        } else {
            protocol.registerServerbound(type, null, handler);
        }
    }

    private static void setHandler(final PacketMapping mapping, final PacketHandler handler) {
        Field handlerField = null;
        for (Field field : mapping.getClass().getDeclaredFields()) {
            if (field.getName().equals("handler")) {
                handlerField = field;
            }
        }
        if (handlerField == null) {
            throw new IllegalStateException("Could not find handler field in PacketMapping");
        }
        handlerField.setAccessible(true);
        try {
            handlerField.set(mapping, handler);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
