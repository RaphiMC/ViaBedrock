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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import net.lenni0451.mcstructs_bedrock.text.components.RootBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.components.TranslationBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.serializer.BedrockComponentSerializer;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.TitleTypes;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.providers.SkinProvider;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class HudPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_LIST, ClientboundPackets1_19_4.PLAYER_INFO_UPDATE, wrapper -> {
            final PlayerListStorage playerListStorage = wrapper.user().get(PlayerListStorage.class);

            final short action = wrapper.read(Type.UNSIGNED_BYTE); // action
            if (action == 0) { // ADD
                final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // length
                final UUID[] uuids = new UUID[length];
                final String[] names = new String[length];
                final BitSet actions = new BitSet(6);
                actions.set(0); // ADD_PLAYER
                actions.set(3); // UPDATE_LISTED
                actions.set(5); // UPDATE_DISPLAY_NAME
                wrapper.write(new BitSetType(6), actions); // actions
                wrapper.write(Type.VAR_INT, length); // length
                for (int i = 0; i < length; i++) {
                    uuids[i] = wrapper.read(BedrockTypes.UUID); // uuid
                    wrapper.write(Type.UUID, uuids[i]); // uuid
                    wrapper.read(BedrockTypes.VAR_LONG); // entity id
                    names[i] = wrapper.read(BedrockTypes.STRING); // username
                    wrapper.write(Type.STRING, StringUtil.encodeUUID(uuids[i])); // username
                    wrapper.write(Type.VAR_INT, 5); // property count
                    wrapper.write(Type.STRING, "xuid"); // property name
                    wrapper.write(Type.STRING, wrapper.read(BedrockTypes.STRING)); // xuid
                    wrapper.write(Type.OPTIONAL_STRING, null); // signature
                    wrapper.write(Type.STRING, "platform_chat_id"); // property name
                    wrapper.write(Type.STRING, wrapper.read(BedrockTypes.STRING)); // platform chat id
                    wrapper.write(Type.OPTIONAL_STRING, null); // signature
                    wrapper.write(Type.STRING, "device_os"); // property name
                    wrapper.write(Type.STRING, wrapper.read(BedrockTypes.INT_LE).toString()); // device os
                    wrapper.write(Type.OPTIONAL_STRING, null); // signature
                    final SkinData skin = wrapper.read(BedrockTypes.SKIN); // skin
                    wrapper.write(Type.STRING, "is_teacher"); // property name
                    wrapper.write(Type.STRING, wrapper.read(Type.BOOLEAN).toString()); // is teacher
                    wrapper.write(Type.OPTIONAL_STRING, null); // signature
                    wrapper.write(Type.STRING, "is_host"); // property name
                    wrapper.write(Type.STRING, wrapper.read(Type.BOOLEAN).toString()); // is host
                    wrapper.write(Type.OPTIONAL_STRING, null); // signature

                    wrapper.write(Type.BOOLEAN, true); // listed
                    wrapper.write(Type.OPTIONAL_COMPONENT, JsonUtil.textToComponent(names[i])); // display name

                    Via.getManager().getProviders().get(SkinProvider.class).setSkin(wrapper.user(), uuids[i], skin);
                }
                for (int i = 0; i < length; i++) {
                    wrapper.read(Type.BOOLEAN); // trusted skin
                }

                final List<UUID> toRemoveUUIDs = new ArrayList<>();
                final List<String> toRemoveNames = new ArrayList<>();
                for (int i = 0; i < uuids.length; i++) {
                    final String name = playerListStorage.addPlayer(uuids[i], names[i]);
                    if (name != null) {
                        toRemoveUUIDs.add(uuids[i]);
                        toRemoveNames.add(name);
                    }
                }
                if (!toRemoveUUIDs.isEmpty()) {
                    // Remove duplicate players from the player list first because Mojang client overwrites entries if they are added twice
                    final PacketWrapper playerInfoRemove = PacketWrapper.create(ClientboundPackets1_19_4.PLAYER_INFO_REMOVE, wrapper.user());
                    playerInfoRemove.write(Type.UUID_ARRAY, toRemoveUUIDs.toArray(new UUID[0])); // uuids
                    playerInfoRemove.send(BedrockProtocol.class);

                    final PacketWrapper customChatCompletions = PacketWrapper.create(ClientboundPackets1_19_4.CUSTOM_CHAT_COMPLETIONS, wrapper.user());
                    customChatCompletions.write(Type.VAR_INT, 1); // action | 1 = REMOVE
                    customChatCompletions.write(Type.STRING_ARRAY, toRemoveNames.toArray(new String[0])); // entries
                    customChatCompletions.send(BedrockProtocol.class);
                }

                final PacketWrapper customChatCompletions = PacketWrapper.create(ClientboundPackets1_19_4.CUSTOM_CHAT_COMPLETIONS, wrapper.user());
                customChatCompletions.write(Type.VAR_INT, 0); // action | 0 = ADD
                customChatCompletions.write(Type.STRING_ARRAY, names); // entries
                customChatCompletions.send(BedrockProtocol.class);
            } else if (action == 1) { // REMOVE
                wrapper.setPacketType(ClientboundPackets1_19_4.PLAYER_INFO_REMOVE);
                final UUID[] uuids = wrapper.read(BedrockTypes.UUID_ARRAY); // uuids
                wrapper.write(Type.UUID_ARRAY, uuids); // uuids

                final List<String> names = new ArrayList<>();
                for (UUID uuid : uuids) {
                    final String name = playerListStorage.removePlayer(uuid);
                    if (name != null) {
                        names.add(name);
                    }
                }

                final PacketWrapper customChatCompletions = PacketWrapper.create(ClientboundPackets1_19_4.CUSTOM_CHAT_COMPLETIONS, wrapper.user());
                customChatCompletions.write(Type.VAR_INT, 1); // action | 1 = REMOVE
                customChatCompletions.write(Type.STRING_ARRAY, names.toArray(new String[0])); // entries
                customChatCompletions.send(BedrockProtocol.class);
            } else {
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Unsupported player list action: " + action);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_TITLE, null, wrapper -> {
            final int type = wrapper.read(BedrockTypes.VAR_INT); // type
            String text = wrapper.read(BedrockTypes.STRING); // text
            final int fadeInTicks = wrapper.read(BedrockTypes.VAR_INT); // fade in ticks
            final int stayTicks = wrapper.read(BedrockTypes.VAR_INT); // stay ticks
            final int fadeOutTicks = wrapper.read(BedrockTypes.VAR_INT); // fade out ticks
            wrapper.read(BedrockTypes.STRING); // xuid
            wrapper.read(BedrockTypes.STRING); // platform chat id

            final Function<String, String> translator = k -> BedrockProtocol.MAPPINGS.getTranslations().getOrDefault(k, k);
            final String originalText = text;
            try {
                if (type >= TitleTypes.TITLE_JSON && type <= TitleTypes.ACTIONBAR_JSON) {
                    final RootBedrockComponent rootComponent = BedrockComponentSerializer.deserialize(text);
                    rootComponent.forEach(c -> {
                        if (c instanceof TranslationBedrockComponent) ((TranslationBedrockComponent) c).setTranslator(translator);
                    });
                    text = rootComponent.asString();
                }

                switch (type) {
                    case TitleTypes.CLEAR:
                    case TitleTypes.RESET:
                        wrapper.setPacketType(ClientboundPackets1_19_4.CLEAR_TITLES);
                        wrapper.write(Type.BOOLEAN, type == TitleTypes.RESET); // reset
                        break;
                    case TitleTypes.TITLE:
                    case TitleTypes.TITLE_JSON:
                        wrapper.setPacketType(ClientboundPackets1_19_4.TITLE_TEXT);
                        wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(text)); // text
                        break;
                    case TitleTypes.SUBTITLE:
                    case TitleTypes.SUBTITLE_JSON:
                        wrapper.setPacketType(ClientboundPackets1_19_4.TITLE_SUBTITLE);
                        wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(text)); // text
                        break;
                    case TitleTypes.ACTIONBAR:
                    case TitleTypes.ACTIONBAR_JSON:
                        wrapper.setPacketType(ClientboundPackets1_19_4.ACTIONBAR);
                        wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(text)); // text
                        break;
                    case TitleTypes.TIMES:
                        wrapper.setPacketType(ClientboundPackets1_19_4.TITLE_TIMES);
                        wrapper.write(Type.INT, fadeInTicks); // fade in ticks
                        wrapper.write(Type.INT, stayTicks); // stay ticks
                        wrapper.write(Type.INT, fadeOutTicks); // fade out ticks
                        break;
                    default:
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown title type: " + type);
                        wrapper.cancel();
                }
            } catch (Throwable e) { // Mojang client silently ignores errors
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while translating '" + originalText + "'", e);
                wrapper.cancel();
            }
        });
    }

}
