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

import com.google.common.collect.Sets;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.lenni0451.mcstructs_bedrock.text.components.RootBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.components.TranslationBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.serializer.BedrockComponentSerializer;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.lenni0451.mcstructs_bedrock.text.utils.TranslatorOptions;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.*;
import net.raphimc.viabedrock.protocol.model.CommandData;
import net.raphimc.viabedrock.protocol.model.CommandOriginData;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class ChatPackets {

    private static final PacketHandler CHAT_COMMAND_HANDLER = new PacketHandlers() {
        @Override
        protected void register() {
            map(Types.STRING, BedrockTypes.STRING, c -> '/' + c); // command
            handler(wrapper -> wrapper.write(BedrockTypes.COMMAND_ORIGIN_DATA, new CommandOriginData(CommandOriginType.Player, UUID.randomUUID(), ""))); // origin
            create(Types.BOOLEAN, false); // is internal
            create(BedrockTypes.STRING, ProtocolConstants.BEDROCK_COMMAND_VERSION); // version
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
                    final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
                    if (!gameSession.areCommandsEnabled() || (gameSession.getChatRestrictionLevel() == ChatRestrictionLevel.Disabled && clientPlayer.abilities().playerPermission() <= PlayerPermissionLevel.Member.getValue())) {
                        wrapper.cancel();
                        PacketFactory.sendJavaSystemChat(wrapper.user(), TextUtil.stringToNbt("§e" + wrapper.user().get(ResourcePacksStorage.class).getTexts().get("commands.generic.disabled")));
                    }
                }
            });
        }
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.TEXT, ClientboundPackets1_21_11.SYSTEM_CHAT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final boolean localize = wrapper.read(Types.BOOLEAN); // localize
                    final short messageType = wrapper.read(Types.UNSIGNED_BYTE); // message type
                    switch (messageType) {
                        case 0 -> {
                            for (int i = 0; i < 6; i++) {
                                wrapper.read(BedrockTypes.STRING); // unused
                            }
                        }
                        case 1, 2 -> {
                            for (int i = 0; i < 3; i++) {
                                wrapper.read(BedrockTypes.STRING); // unused
                            }
                        }
                        default -> throw new IllegalStateException("Unhandled message type: " + messageType);
                    }
                    final short rawType = wrapper.read(Types.UNSIGNED_BYTE); // text packet type
                    final TextPacketType type = TextPacketType.getByValue(rawType);
                    if (type == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown TextPacketType: " + rawType);
                        wrapper.cancel();
                        return;
                    }

                    final Function<String, String> translator = wrapper.user().get(ResourcePacksStorage.class).getTexts().lookup();
                    String originalMessage = null;
                    try {
                        switch (type) {
                            case Chat, Whisper, Announcement -> {
                                final String sourceName = wrapper.read(BedrockTypes.STRING); // source name
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                if (localize) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                if (type == TextPacketType.Chat && !sourceName.isEmpty()) {
                                    message = BedrockTranslator.translate("chat.type.text", translator, new String[]{sourceName, message}, TranslatorOptions.SKIP_ARGS_TRANSLATION);
                                } else if (type == TextPacketType.Whisper) {
                                    message = BedrockTranslator.translate("chat.type.text", translator, new String[]{sourceName, BedrockTranslator.translate("§7§o%commands.message.display.incoming", translator, new String[]{sourceName, message})}, TranslatorOptions.SKIP_ARGS_TRANSLATION);
                                }

                                wrapper.write(Types.TAG, TextUtil.stringToNbt(message));
                                wrapper.write(Types.BOOLEAN, false); // overlay
                            }
                            case TextObjectWhisper, TextObject, TextObjectAnnouncement -> {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                final RootBedrockComponent rootComponent = BedrockComponentSerializer.deserialize(message);
                                rootComponent.forEach(c -> {
                                    if (c instanceof TranslationBedrockComponent) ((TranslationBedrockComponent) c).setTranslator(translator);
                                });
                                message = rootComponent.asString();
                                if (localize) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                wrapper.write(Types.TAG, TextUtil.stringToNbt(message)); // message
                                wrapper.write(Types.BOOLEAN, false); // overlay
                            }
                            case Raw, SystemMessage, Tip -> {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                if (localize) {
                                    message = BedrockTranslator.translate(message, translator, new Object[0]);
                                }

                                wrapper.write(Types.TAG, TextUtil.stringToNbt(message)); // message
                                wrapper.write(Types.BOOLEAN, type == TextPacketType.Tip); // overlay
                            }
                            case Translate, Popup, JukeboxPopup -> {
                                String message = originalMessage = wrapper.read(BedrockTypes.STRING); // message
                                final String[] parameters = wrapper.read(BedrockTypes.STRING_ARRAY); // parameters
                                if (localize) {
                                    message = BedrockTranslator.translate(message, translator, parameters);
                                }

                                wrapper.write(Types.TAG, TextUtil.stringToNbt(message)); // message
                                wrapper.write(Types.BOOLEAN, type == TextPacketType.Popup || type == TextPacketType.JukeboxPopup); // overlay
                            }
                            default -> throw new IllegalStateException("Unhandled TextPacketType: " + type);
                        }
                    } catch (Throwable e) { // Bedrock client silently ignores errors
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while translating '" + originalMessage + "'", e);
                        wrapper.cancel();
                    }
                });
                read(BedrockTypes.STRING); // xuid
                read(BedrockTypes.STRING); // platform online id
                read(BedrockTypes.OPTIONAL_STRING); // filtered message
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.COMMAND_OUTPUT, ClientboundPackets1_21_11.SYSTEM_CHAT, wrapper -> {
            final CommandOriginData originData = wrapper.read(BedrockTypes.COMMAND_ORIGIN_DATA); // origin
            final String rawType = wrapper.read(BedrockTypes.STRING); // type
            final CommandOutputType type = CommandOutputType.getByName(rawType); // type
            if (type == null) { // Bedrock client disconnects if the type is not valid
                throw new IllegalStateException("Unknown CommandOutputType: " + rawType);
            }
            wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // success count

            if (originData.type() != CommandOriginType.Player) { // Bedrock client ignores non player origins
                wrapper.cancel();
                return;
            }

            final Function<String, String> translator = wrapper.user().get(ResourcePacksStorage.class).getTexts().lookup();
            final StringBuilder message = new StringBuilder();
            final int messageCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // message count
            for (int i = 0; i < messageCount; i++) {
                final String messageId = wrapper.read(BedrockTypes.STRING); // message id
                final boolean successful = wrapper.read(Types.BOOLEAN); // is successful
                final String[] parameters = wrapper.read(BedrockTypes.STRING_ARRAY); // parameters

                message.append(successful ? "§r" : "§c");
                message.append(BedrockTranslator.translate(messageId, translator, parameters));
                if (i != messageCount - 1) {
                    message.append("\n");
                }
            }
            wrapper.read(BedrockTypes.OPTIONAL_STRING); // data set

            wrapper.write(Types.TAG, TextUtil.stringToNbt(message.toString()));
            wrapper.write(Types.BOOLEAN, false); // overlay
        });
        protocol.registerClientboundTransition(ClientboundBedrockPackets.AVAILABLE_COMMANDS,
                State.CONFIGURATION, (PacketHandler) wrapper -> {
                    final CommandData[] commands = wrapper.read(BedrockTypes.COMMAND_DATA_ARRAY); // commands
                    wrapper.user().put(new CommandsStorage(wrapper.user(), commands));
                    wrapper.cancel(); // Will be sent when the java player is ready
                }, ClientboundPackets1_21_11.COMMANDS, (PacketHandler) wrapper -> {
                    final CommandData[] commands = wrapper.read(BedrockTypes.COMMAND_DATA_ARRAY); // commands
                    final CommandsStorage commandsStorage = new CommandsStorage(wrapper.user(), commands);
                    wrapper.user().put(commandsStorage);
                    commandsStorage.writeCommandTree(wrapper);
                }
        );
        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_SOFT_ENUM, null, wrapper -> {
            wrapper.cancel();
            final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
            if (commandsStorage == null) return;

            final String name = wrapper.read(BedrockTypes.STRING); // name
            final Set<String> values = Sets.newHashSet(wrapper.read(BedrockTypes.STRING_ARRAY)); // values

            final CommandData.EnumData softEnum = commandsStorage.getSoftEnum(name);
            if (softEnum == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received update for unknown soft enum: " + name);
                return;
            }

            final byte rawAction = wrapper.read(Types.BYTE); // action
            final SoftEnumUpdateType action = SoftEnumUpdateType.getByValue(rawAction);
            if (action == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown SoftEnumUpdateType: " + rawAction);
                return;
            }

            switch (action) {
                case Add -> softEnum.addValues(values);
                case Remove -> softEnum.removeValues(values);
                case Replace -> {
                    softEnum.values().clear();
                    softEnum.addValues(values);
                }
                default -> throw new IllegalStateException("Unhandled SoftEnumUpdateType: " + action);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_COMMANDS_ENABLED, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final boolean commandsEnabled = wrapper.read(Types.BOOLEAN); // commands enabled
            if (commandsEnabled != gameSession.areCommandsEnabled()) {
                gameSession.setCommandsEnabled(commandsEnabled);
                final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
                if (commandsStorage != null) {
                    commandsStorage.updateCommandTree();
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_21_6.CHAT, ServerboundBedrockPackets.TEXT, new PacketHandlers() {
            @Override
            public void register() {
                create(Types.BOOLEAN, false); // localize
                create(Types.UNSIGNED_BYTE, (short) 1); // message type
                create(BedrockTypes.STRING, "chat"); // dummy string 1
                create(BedrockTypes.STRING, "whisper"); // dummy string 2
                create(BedrockTypes.STRING, "announcement"); // dummy string 3
                create(Types.UNSIGNED_BYTE, (short) TextPacketType.Chat.getValue()); // type
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().get(EntityTracker.class).getClientPlayer().name())); // source name
                map(Types.STRING, BedrockTypes.STRING); // message
                handler(wrapper -> wrapper.write(BedrockTypes.STRING, wrapper.user().get(AuthData.class).getXuid())); // xuid
                create(BedrockTypes.STRING, ""); // platform online id
                create(BedrockTypes.OPTIONAL_STRING, null); // filtered message
                handler(PacketWrapper::clearInputBuffer);
                handler(wrapper -> {
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
                    final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
                    if (gameSession.getChatRestrictionLevel() != ChatRestrictionLevel.None || clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Muted)) {
                        wrapper.cancel();
                        PacketFactory.sendJavaSystemChat(wrapper.user(), TextUtil.stringToNbt("§e" + wrapper.user().get(ResourcePacksStorage.class).getTexts().get("permissions.chatmute")));
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.CHAT_COMMAND, ServerboundBedrockPackets.COMMAND_REQUEST, CHAT_COMMAND_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_21_6.CHAT_COMMAND_SIGNED, ServerboundBedrockPackets.COMMAND_REQUEST, CHAT_COMMAND_HANDLER);
        protocol.registerServerbound(ServerboundPackets1_21_6.COMMAND_SUGGESTION, null, wrapper -> {
            wrapper.cancel();
            final CommandsStorage commandsStorage = wrapper.user().get(CommandsStorage.class);
            if (commandsStorage == null) return;

            final int id = wrapper.read(Types.VAR_INT); // transaction id
            final String command = wrapper.read(Types.STRING); // command
            if (!command.startsWith("/")) {
                return;
            }

            final Suggestions suggestions = commandsStorage.complete(command);

            final PacketWrapper tabComplete = PacketWrapper.create(ClientboundPackets1_21_11.COMMAND_SUGGESTIONS, wrapper.user());
            tabComplete.write(Types.VAR_INT, id); // transaction id
            tabComplete.write(Types.VAR_INT, suggestions.getRange().getStart()); // start index
            tabComplete.write(Types.VAR_INT, suggestions.getRange().getLength()); // length
            tabComplete.write(Types.VAR_INT, suggestions.getList().size()); // count
            for (Suggestion suggestion : suggestions.getList()) {
                tabComplete.write(Types.STRING, suggestion.getText()); // text
                if (suggestion.getTooltip() != null) {
                    tabComplete.write(Types.OPTIONAL_TAG, TextUtil.stringToNbt(suggestion.getTooltip().getString())); // tooltip
                } else {
                    tabComplete.write(Types.OPTIONAL_TAG, null); // tooltip
                }
            }
            tabComplete.send(BedrockProtocol.class);
        });
    }

}
