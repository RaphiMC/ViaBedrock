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

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.BitSetType;
import com.viaversion.viaversion.api.type.types.ByteArrayType;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.lenni0451.mcstructs_bedrock.text.components.RootBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.components.TranslationBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.serializer.BedrockComponentSerializer;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.lenni0451.mcstructs_bedrock.text.utils.TranslatorOptions;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandOriginTypes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandOutputType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.TextType;
import net.raphimc.viabedrock.protocol.model.CommandOrigin;
import net.raphimc.viabedrock.protocol.storage.AuthChainData;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class ChatPackets {

    private static final ByteArrayType.OptionalByteArrayType OPTIONAL_MESSAGE_SIGNATURE_BYTES_TYPE = new ByteArrayType.OptionalByteArrayType(256);
    private static final ByteArrayType MESSAGE_SIGNATURE_BYTES_TYPE = new ByteArrayType(256);
    private static final BitSetType ACKNOWLEDGED_BIT_SET_TYPE = new BitSetType(20);

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.TEXT, ClientboundPackets1_19_3.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final short type = wrapper.read(Type.UNSIGNED_BYTE); // type
                    final boolean needsTranslation = wrapper.read(Type.BOOLEAN); // needs translation

                    final Function<String, String> translator = k -> BedrockProtocol.MAPPINGS.getTranslations().getOrDefault(k, k);
                    String originalMessage = null;
                    try {
                        switch (type) {
                            case TextType.CHAT:
                            case TextType.WHISPER:
                            case TextType.ANNOUNCEMENT: {
                                final String sourceName = wrapper.read(BedrockTypes.STRING); // source name
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                if (type == TextType.CHAT && !sourceName.isEmpty()) {
                                    message = BedrockTranslator.translate("chat.type.text", translator, new String[]{sourceName, message}, TranslatorOptions.SKIP_ARGS_TRANSLATION);
                                } else if (type == TextType.WHISPER) {
                                    message = BedrockTranslator.translate("chat.type.text", translator, new String[]{sourceName, BedrockTranslator.translate("§7§o%commands.message.display.incoming", translator, new String[]{sourceName, message})}, TranslatorOptions.SKIP_ARGS_TRANSLATION);
                                }

                                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(message));
                                wrapper.write(Type.BOOLEAN, false); // overlay
                                break;
                            }
                            case TextType.OBJECT:
                            case TextType.OBJECT_WHISPER:
                            case TextType.OBJECT_ANNOUNCEMENT: {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                final RootBedrockComponent rootComponent = BedrockComponentSerializer.deserialize(message);
                                rootComponent.forEach(c -> {
                                    if (c instanceof TranslationBedrockComponent) ((TranslationBedrockComponent) c).setTranslator(translator);
                                });
                                message = rootComponent.asString();
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(message)); // message
                                wrapper.write(Type.BOOLEAN, false); // overlay
                                break;
                            }
                            case TextType.RAW:
                            case TextType.SYSTEM:
                            case TextType.TIP: {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(message)); // message
                                wrapper.write(Type.BOOLEAN, type == TextType.TIP); // overlay
                                break;
                            }
                            case TextType.TRANSLATION:
                            case TextType.POPUP:
                            case TextType.JUKEBOX_POPUP: {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                final String[] parameters = wrapper.read(BedrockTypes.STRING_ARRAY); // parameters
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, parameters);
                                }

                                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(message)); // message
                                wrapper.write(Type.BOOLEAN, type == TextType.POPUP || type == TextType.JUKEBOX_POPUP); // overlay
                                break;
                            }
                            default:
                                BedrockProtocol.kickForIllegalState(wrapper.user(), "Unknown text type: " + type);
                        }
                    } catch (Throwable e) { // Mojang client silently ignores errors
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while translating '" + originalMessage + "' (" + type + ")", e);
                        wrapper.cancel();
                    }
                });
                read(BedrockTypes.STRING); // xuid
                read(BedrockTypes.STRING); // platform chat id
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.COMMAND_OUTPUT, ClientboundPackets1_19_3.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final CommandOrigin originData = wrapper.read(BedrockTypes.COMMAND_ORIGIN); // origin
                    final short type = wrapper.read(Type.UNSIGNED_BYTE); // type
                    wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // success count

                    if (type != CommandOutputType.ALL_OUTPUT) { // TODO: handle other types
                        BedrockProtocol.kickForIllegalState(wrapper.user(), "Unhandled command output type: " + type);
                        wrapper.cancel();
                        return;
                    }
                    if (originData.type() != CommandOriginTypes.PLAYER) { // TODO: handle other types
                        BedrockProtocol.kickForIllegalState(wrapper.user(), "Unhandled command origin type: " + originData.type());
                        wrapper.cancel();
                        return;
                    }
                    final Function<String, String> translator = k -> BedrockProtocol.MAPPINGS.getTranslations().getOrDefault(k, k);
                    final StringBuilder message = new StringBuilder();

                    final int messageCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // message count
                    for (int i = 0; i < messageCount; i++) {
                        final boolean internal = wrapper.read(Type.BOOLEAN); // is internal
                        final String messageId = wrapper.read(BedrockTypes.STRING); // message id
                        final String[] parameters = wrapper.read(BedrockTypes.STRING_ARRAY); // parameters

                        message.append(internal ? "§r" : "§c");
                        message.append(BedrockTranslator.translate(messageId, translator, parameters));
                        if (i != messageCount - 1) {
                            message.append("\n");
                        }
                    }
                    if (type == CommandOutputType.DATA_SET) {
                        wrapper.read(BedrockTypes.STRING); // data
                    }

                    wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(message.toString()));
                    wrapper.write(Type.BOOLEAN, false); // overlay
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19_3.CHAT_MESSAGE, ServerboundBedrockPackets.TEXT, new PacketHandlers() {
            @Override
            public void register() {
                create(Type.UNSIGNED_BYTE, TextType.CHAT); // type
                create(Type.BOOLEAN, false); // needs translation
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().getProtocolInfo().getUsername())); // source name
                map(Type.STRING, BedrockTypes.STRING); // message
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().get(AuthChainData.class).getXuid())); // xuid
                create(BedrockTypes.STRING, ""); // platform chat id
                read(Type.LONG); // timestamp
                read(Type.LONG); // salt
                read(OPTIONAL_MESSAGE_SIGNATURE_BYTES_TYPE); // signature
                read(Type.VAR_INT); // offset
                read(ACKNOWLEDGED_BIT_SET_TYPE); // acknowledged
                handler(wrapper -> {
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    if (gameSession.isChatRestricted()) {
                        wrapper.cancel();
                        final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_19_3.SYSTEM_CHAT, wrapper.user());
                        systemChat.write(Type.COMPONENT, JsonUtil.textToComponent("§e" + BedrockProtocol.MAPPINGS.getTranslations().get("permissions.chatmute"))); // message
                        systemChat.write(Type.BOOLEAN, false); // overlay
                        systemChat.send(BedrockProtocol.class);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19_3.CHAT_COMMAND, ServerboundBedrockPackets.COMMAND_REQUEST, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING, BedrockTypes.STRING, c -> "/" + c); // command
                handler(wrapper -> {
                    final UUID uuid = wrapper.user().getProtocolInfo().getUuid();
                    wrapper.write(BedrockTypes.COMMAND_ORIGIN, new CommandOrigin(CommandOriginTypes.PLAYER, uuid, "")); // origin
                });
                create(Type.BOOLEAN, false); // internal
                create(BedrockTypes.VAR_INT, 26); // version
                read(Type.LONG); // timestamp
                read(Type.LONG); // salt
                handler(wrapper -> {
                    final int signatures = wrapper.read(Type.VAR_INT); // count
                    for (int i = 0; i < signatures; i++) {
                        wrapper.read(Type.STRING); // argument name
                        wrapper.read(MESSAGE_SIGNATURE_BYTES_TYPE); // signature
                    }
                });
                read(Type.VAR_INT); // offset
                read(ACKNOWLEDGED_BIT_SET_TYPE); // acknowledged
                handler(wrapper -> {
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    if (!gameSession.areCommandsEnabled()) {
                        wrapper.cancel();
                        final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_19_3.SYSTEM_CHAT, wrapper.user());
                        systemChat.write(Type.COMPONENT, JsonUtil.textToComponent("§e" + BedrockProtocol.MAPPINGS.getTranslations().get("commands.generic.disabled"))); // message
                        systemChat.write(Type.BOOLEAN, false); // overlay
                        systemChat.send(BedrockProtocol.class);
                    }
                });
            }
        });
    }

}
