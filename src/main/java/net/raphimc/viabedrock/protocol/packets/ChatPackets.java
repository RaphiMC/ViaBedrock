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

import com.google.common.collect.Sets;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import net.lenni0451.mcstructs_bedrock.text.components.RootBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.components.TranslationBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.serializer.BedrockComponentSerializer;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.lenni0451.mcstructs_bedrock.text.utils.TranslatorOptions;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ChatRestrictionLevels;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandOutputTypes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerPermissions;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.TextTypes;
import net.raphimc.viabedrock.protocol.model.CommandData;
import net.raphimc.viabedrock.protocol.model.CommandOrigin;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class ChatPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.TEXT, ClientboundPackets1_20_3.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final short type = wrapper.read(Type.UNSIGNED_BYTE); // type
                    final boolean needsTranslation = wrapper.read(Type.BOOLEAN); // needs translation

                    final Function<String, String> translator = wrapper.user().get(ResourcePacksStorage.class).getTranslationLookup();
                    String originalMessage = null;
                    try {
                        switch (type) {
                            case TextTypes.CHAT:
                            case TextTypes.WHISPER:
                            case TextTypes.ANNOUNCEMENT: {
                                final String sourceName = wrapper.read(BedrockTypes.STRING); // source name
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                if (type == TextTypes.CHAT && !sourceName.isEmpty()) {
                                    message = BedrockTranslator.translate("chat.type.text", translator, new String[]{sourceName, message}, TranslatorOptions.SKIP_ARGS_TRANSLATION);
                                } else if (type == TextTypes.WHISPER) {
                                    message = BedrockTranslator.translate("chat.type.text", translator, new String[]{sourceName, BedrockTranslator.translate("§7§o%commands.message.display.incoming", translator, new String[]{sourceName, message})}, TranslatorOptions.SKIP_ARGS_TRANSLATION);
                                }

                                wrapper.write(Type.TAG, TextUtil.stringToNbt(message));
                                wrapper.write(Type.BOOLEAN, false); // overlay
                                break;
                            }
                            case TextTypes.OBJECT:
                            case TextTypes.OBJECT_WHISPER:
                            case TextTypes.OBJECT_ANNOUNCEMENT: {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                final RootBedrockComponent rootComponent = BedrockComponentSerializer.deserialize(message);
                                rootComponent.forEach(c -> {
                                    if (c instanceof TranslationBedrockComponent) ((TranslationBedrockComponent) c).setTranslator(translator);
                                });
                                message = rootComponent.asString();
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                wrapper.write(Type.TAG, TextUtil.stringToNbt(message)); // message
                                wrapper.write(Type.BOOLEAN, false); // overlay
                                break;
                            }
                            case TextTypes.RAW:
                            case TextTypes.SYSTEM:
                            case TextTypes.TIP: {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                wrapper.write(Type.TAG, TextUtil.stringToNbt(message)); // message
                                wrapper.write(Type.BOOLEAN, type == TextTypes.TIP); // overlay
                                break;
                            }
                            case TextTypes.TRANSLATION:
                            case TextTypes.POPUP:
                            case TextTypes.JUKEBOX_POPUP: {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                final String[] parameters = wrapper.read(BedrockTypes.STRING_ARRAY); // parameters
                                if (needsTranslation) {
                                    message = BedrockTranslator.translate(message, translator, parameters);
                                }

                                wrapper.write(Type.TAG, TextUtil.stringToNbt(message)); // message
                                wrapper.write(Type.BOOLEAN, type == TextTypes.POPUP || type == TextTypes.JUKEBOX_POPUP); // overlay
                                break;
                            }
                            default: // Mojang client silently ignores unknown actions
                                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown text type: " + type);
                                wrapper.cancel();
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
        protocol.registerClientbound(ClientboundBedrockPackets.COMMAND_OUTPUT, ClientboundPackets1_20_3.SYSTEM_CHAT, wrapper -> {
            final CommandOrigin originData = wrapper.read(BedrockTypes.COMMAND_ORIGIN); // origin
            final short type = wrapper.read(Type.UNSIGNED_BYTE); // type
            wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // success count

            if (type != CommandOutputTypes.ALL_OUTPUT) { // TODO: Handle other types
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Unhandled command output type: " + type);
                wrapper.cancel();
                return;
            }
            if (originData.type() != CommandOrigin.TYPE_PLAYER) { // TODO: Handle other types
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Unhandled command origin type: " + originData.type());
                wrapper.cancel();
                return;
            }
            final Function<String, String> translator = wrapper.user().get(ResourcePacksStorage.class).getTranslationLookup();
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
            if (type == CommandOutputTypes.DATA_SET) {
                wrapper.read(BedrockTypes.STRING); // data
            }

            wrapper.write(Type.TAG, TextUtil.stringToNbt(message.toString()));
            wrapper.write(Type.BOOLEAN, false); // overlay
        });
        protocol.registerClientbound(ClientboundBedrockPackets.AVAILABLE_COMMANDS, ClientboundPackets1_20_3.DECLARE_COMMANDS, wrapper -> {
            final CommandData[] commands = wrapper.read(BedrockTypes.COMMAND_DATA_ARRAY); // commands
            final CommandsStorage commandsStorage = new CommandsStorage(wrapper.user(), commands);
            wrapper.user().put(commandsStorage);
            commandsStorage.writeCommandTree(wrapper);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_SOFT_ENUM, null, wrapper -> {
            wrapper.cancel();
            final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
            if (commandsStorage == null) return;

            final String name = wrapper.read(BedrockTypes.STRING); // name
            final Set<String> values = Sets.newHashSet(wrapper.read(BedrockTypes.STRING_ARRAY)); // values

            final CommandData.EnumData dynamicEnum = commandsStorage.getDynamicEnum(name);
            if (dynamicEnum == null) { // Mojang client silently ignores unknown enums
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received update for unknown dynamic enum: " + name);
                return;
            }

            final byte action = wrapper.read(Type.BYTE); // action
            if (action == 0) { // ADD
                dynamicEnum.addValues(values);
            } else if (action == 1) { // REMOVE
                dynamicEnum.removeValues(values);
            } else if (action == 2) { // REPLACE
                dynamicEnum.values().clear();
                dynamicEnum.addValues(values);
            } else { // Mojang client silently ignores unknown actions
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown dynamic enum action: " + action);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_20_3.CHAT_MESSAGE, ServerboundBedrockPackets.TEXT, new PacketHandlers() {
            @Override
            public void register() {
                create(Type.UNSIGNED_BYTE, TextTypes.CHAT); // type
                create(Type.BOOLEAN, false); // needs translation
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().get(EntityTracker.class).getClientPlayer().name())); // source name
                map(Type.STRING, BedrockTypes.STRING); // message
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().get(AuthChainData.class).getXuid())); // xuid
                create(BedrockTypes.STRING, ""); // platform chat id
                handler(PacketWrapper::clearInputBuffer);
                handler(wrapper -> {
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    if (gameSession.getChatRestrictionLevel() >= ChatRestrictionLevels.COMMANDS_ONLY) {
                        wrapper.cancel();
                        PacketFactory.sendSystemChat(wrapper.user(), TextUtil.stringToNbt("§e" + wrapper.user().get(ResourcePacksStorage.class).getTranslations().get("permissions.chatmute")));
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.CHAT_COMMAND, ServerboundBedrockPackets.COMMAND_REQUEST, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING, BedrockTypes.STRING, c -> '/' + c); // command
                handler(wrapper -> {
                    final UUID uuid = wrapper.user().getProtocolInfo().getUuid();
                    wrapper.write(BedrockTypes.COMMAND_ORIGIN, new CommandOrigin(CommandOrigin.TYPE_PLAYER, uuid, "")); // origin
                });
                create(Type.BOOLEAN, false); // internal
                create(BedrockTypes.VAR_INT, ProtocolConstants.BEDROCK_COMMAND_VERSION); // version
                handler(PacketWrapper::clearInputBuffer);
                handler(wrapper -> {
                    final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
                    int execResult = CommandsStorage.RESULT_NO_OP;
                    if (commandsStorage != null) {
                        execResult = commandsStorage.execute(wrapper.get(BedrockTypes.STRING, 0));
                    }

                    if (execResult == CommandsStorage.RESULT_CANCEL) {
                        wrapper.cancel();
                    } else if (execResult != CommandsStorage.RESULT_ALLOW_SEND) {
                        final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                        if (!gameSession.areCommandsEnabled() || (gameSession.getChatRestrictionLevel() >= ChatRestrictionLevels.HIDDEN && gameSession.getPlayerPermission() <= PlayerPermissions.OPERATOR)) {
                            wrapper.cancel();
                            PacketFactory.sendSystemChat(wrapper.user(), TextUtil.stringToNbt("§e" + wrapper.user().get(ResourcePacksStorage.class).getTranslations().get("commands.generic.disabled")));
                        }
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_3.TAB_COMPLETE, null, wrapper -> {
            wrapper.cancel();
            final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
            if (commandsStorage == null) return;

            final int id = wrapper.read(Type.VAR_INT); // transaction id
            final String command = wrapper.read(Type.STRING); // command
            if (!command.startsWith("/")) {
                return;
            }

            final Suggestions suggestions = commandsStorage.complete(command);

            final PacketWrapper tabComplete = PacketWrapper.create(ClientboundPackets1_20_3.TAB_COMPLETE, wrapper.user());
            tabComplete.write(Type.VAR_INT, id); // transaction id
            tabComplete.write(Type.VAR_INT, suggestions.getRange().getStart()); // start index
            tabComplete.write(Type.VAR_INT, suggestions.getRange().getLength()); // length
            tabComplete.write(Type.VAR_INT, suggestions.getList().size()); // count
            for (Suggestion suggestion : suggestions.getList()) {
                tabComplete.write(Type.STRING, suggestion.getText()); // text
                if (suggestion.getTooltip() != null) {
                    tabComplete.write(Type.OPTIONAL_TAG, TextUtil.stringToNbt(suggestion.getTooltip().getString())); // tooltip
                } else {
                    tabComplete.write(Type.OPTIONAL_TAG, null); // tooltip
                }
            }
            tabComplete.send(BedrockProtocol.class);
        });
    }

}
