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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.util.Pair;
import net.lenni0451.mcstructs_bedrock.text.components.RootBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.components.TranslationBedrockComponent;
import net.lenni0451.mcstructs_bedrock.text.serializer.BedrockComponentSerializer;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.scoreboard.ScoreboardEntry;
import net.raphimc.viabedrock.api.model.scoreboard.ScoreboardObjective;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.data.enums.java.CustomChatCompletionsAction;
import net.raphimc.viabedrock.protocol.data.enums.java.ObjectiveCriteriaRenderType;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerInfoUpdateAction;
import net.raphimc.viabedrock.protocol.data.enums.java.ScoreboardObjectiveAction;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.provider.SkinProvider;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.PlayerListStorage;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.storage.ScoreboardTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class HudPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_LIST, ClientboundPackets1_21.PLAYER_INFO_UPDATE, wrapper -> {
            final PlayerListStorage playerListStorage = wrapper.user().get(PlayerListStorage.class);
            final ScoreboardTracker scoreboardTracker = wrapper.user().get(ScoreboardTracker.class);

            final byte rawAction = wrapper.read(Types.BYTE); // action
            final PlayerListPacketType action = PlayerListPacketType.getByValue(rawAction);
            if (action == null) { // Mojang client crashes if the action is not valid
                throw new IllegalStateException("Unknown PlayerListPacketType: " + rawAction);
            }
            switch (action) {
                case Add: {
                    final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // length
                    final UUID[] uuids = new UUID[length];
                    final long[] uniqueEntityIds = new long[length];
                    final String[] names = new String[length];
                    wrapper.write(Types.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.ADD_PLAYER.ordinal(), PlayerInfoUpdateAction.UPDATE_LISTED.ordinal(), PlayerInfoUpdateAction.UPDATE_DISPLAY_NAME.ordinal())); // actions
                    wrapper.write(Types.VAR_INT, length); // length
                    for (int i = 0; i < length; i++) {
                        uuids[i] = wrapper.read(BedrockTypes.UUID); // uuid
                        wrapper.write(Types.UUID, uuids[i]); // uuid
                        uniqueEntityIds[i] = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
                        names[i] = wrapper.read(BedrockTypes.STRING); // username
                        wrapper.write(Types.STRING, StringUtil.encodeUUID(uuids[i])); // username
                        wrapper.write(Types.VAR_INT, 6); // property count
                        wrapper.write(Types.STRING, "xuid"); // property name
                        wrapper.write(Types.STRING, wrapper.read(BedrockTypes.STRING)); // xuid
                        wrapper.write(Types.OPTIONAL_STRING, null); // signature
                        wrapper.write(Types.STRING, "platform_chat_id"); // property name
                        wrapper.write(Types.STRING, wrapper.read(BedrockTypes.STRING)); // platform chat id
                        wrapper.write(Types.OPTIONAL_STRING, null); // signature
                        wrapper.write(Types.STRING, "device_os"); // property name
                        wrapper.write(Types.STRING, wrapper.read(BedrockTypes.INT_LE).toString()); // device os
                        wrapper.write(Types.OPTIONAL_STRING, null); // signature
                        final SkinData skin = wrapper.read(BedrockTypes.SKIN); // skin
                        wrapper.write(Types.STRING, "is_teacher"); // property name
                        wrapper.write(Types.STRING, wrapper.read(Types.BOOLEAN).toString()); // is teacher
                        wrapper.write(Types.OPTIONAL_STRING, null); // signature
                        wrapper.write(Types.STRING, "is_host"); // property name
                        wrapper.write(Types.STRING, wrapper.read(Types.BOOLEAN).toString()); // is host
                        wrapper.write(Types.OPTIONAL_STRING, null); // signature
                        wrapper.write(Types.STRING, "is_subclient"); // property name
                        wrapper.write(Types.STRING, wrapper.read(Types.BOOLEAN).toString()); // is host
                        wrapper.write(Types.OPTIONAL_STRING, null); // signature

                        wrapper.write(Types.BOOLEAN, true); // listed
                        wrapper.write(Types.OPTIONAL_TAG, TextUtil.stringToNbt(names[i])); // display name

                        Via.getManager().getProviders().get(SkinProvider.class).setSkin(wrapper.user(), uuids[i], skin);
                    }
                    for (int i = 0; i < length; i++) {
                        wrapper.read(Types.BOOLEAN); // trusted skin
                    }

                    final List<UUID> toRemoveUUIDs = new ArrayList<>();
                    final List<String> toRemoveNames = new ArrayList<>();
                    for (int i = 0; i < uuids.length; i++) {
                        final Pair<Long, String> entry = playerListStorage.addPlayer(uuids[i], uniqueEntityIds[i], names[i]);
                        if (entry != null) {
                            toRemoveUUIDs.add(uuids[i]);
                            toRemoveNames.add(entry.value());
                        }

                        final Pair<ScoreboardObjective, ScoreboardEntry> scoreboardEntry = scoreboardTracker.getEntryForPlayer(uniqueEntityIds[i]);
                        if (scoreboardEntry != null) {
                            scoreboardEntry.key().updateEntry(wrapper.user(), scoreboardEntry.value());
                        }
                    }

                    if (!toRemoveUUIDs.isEmpty()) {
                        // Remove duplicate players from the player list first because Mojang client overwrites entries if they are added twice
                        final PacketWrapper playerInfoRemove = PacketWrapper.create(ClientboundPackets1_21.PLAYER_INFO_REMOVE, wrapper.user());
                        playerInfoRemove.write(Types.UUID_ARRAY, toRemoveUUIDs.toArray(new UUID[0])); // uuids
                        playerInfoRemove.send(BedrockProtocol.class);

                        PacketFactory.sendJavaCustomChatCompletions(wrapper.user(), CustomChatCompletionsAction.REMOVE, toRemoveNames.toArray(new String[0]));
                    }

                    PacketFactory.sendJavaCustomChatCompletions(wrapper.user(), CustomChatCompletionsAction.ADD, names);
                    break;
                }
                case Remove: {
                    wrapper.setPacketType(ClientboundPackets1_21.PLAYER_INFO_REMOVE);
                    final UUID[] uuids = wrapper.read(BedrockTypes.UUID_ARRAY); // uuids
                    wrapper.write(Types.UUID_ARRAY, uuids); // uuids

                    final List<String> names = new ArrayList<>();
                    for (UUID uuid : uuids) {
                        final Pair<Long, String> entry = playerListStorage.removePlayer(uuid);
                        if (entry != null) {
                            names.add(entry.value());
                            final Pair<ScoreboardObjective, ScoreboardEntry> scoreboardEntry = scoreboardTracker.getEntryForPlayer(entry.key());
                            if (scoreboardEntry != null) {
                                scoreboardEntry.key().updateEntry(wrapper.user(), scoreboardEntry.value());
                            }
                        }
                    }

                    PacketFactory.sendJavaCustomChatCompletions(wrapper.user(), CustomChatCompletionsAction.REMOVE, names.toArray(new String[0]));
                    break;
                }
                default:
                    throw new IllegalStateException("Unhandled PlayerListPacketType: " + action);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_TITLE, null, wrapper -> {
            final int rawType = wrapper.read(BedrockTypes.VAR_INT); // type
            final SetTitlePacket_TitleType type = SetTitlePacket_TitleType.getByValue(rawType);
            if (type == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown SetTitlePacket_TitleType: " + rawType);
                wrapper.cancel();
                return;
            }
            String text = wrapper.read(BedrockTypes.STRING); // text
            final int fadeInTicks = wrapper.read(BedrockTypes.VAR_INT); // fade in ticks
            final int stayTicks = wrapper.read(BedrockTypes.VAR_INT); // stay ticks
            final int fadeOutTicks = wrapper.read(BedrockTypes.VAR_INT); // fade out ticks
            wrapper.read(BedrockTypes.STRING); // xuid
            wrapper.read(BedrockTypes.STRING); // platform chat id

            final Function<String, String> translator = wrapper.user().get(ResourcePacksStorage.class).getTranslationLookup();
            final String originalText = text;
            try {
                if (type.getValue() >= SetTitlePacket_TitleType.TitleTextObject.getValue() && type.getValue() <= SetTitlePacket_TitleType.ActionbarTextObject.getValue()) {
                    final RootBedrockComponent rootComponent = BedrockComponentSerializer.deserialize(text);
                    rootComponent.forEach(c -> {
                        if (c instanceof TranslationBedrockComponent) ((TranslationBedrockComponent) c).setTranslator(translator);
                    });
                    text = rootComponent.asString();
                }

                switch (type) {
                    case Clear:
                    case Reset:
                        wrapper.setPacketType(ClientboundPackets1_21.CLEAR_TITLES);
                        wrapper.write(Types.BOOLEAN, type == SetTitlePacket_TitleType.Reset); // reset
                        break;
                    case Title:
                    case TitleTextObject:
                        wrapper.setPacketType(ClientboundPackets1_21.SET_TITLE_TEXT);
                        wrapper.write(Types.TAG, TextUtil.stringToNbt(text)); // text
                        break;
                    case Subtitle:
                    case SubtitleTextObject:
                        wrapper.setPacketType(ClientboundPackets1_21.SET_SUBTITLE_TEXT);
                        wrapper.write(Types.TAG, TextUtil.stringToNbt(text)); // text
                        break;
                    case Actionbar:
                    case ActionbarTextObject:
                        wrapper.setPacketType(ClientboundPackets1_21.SET_ACTION_BAR_TEXT);
                        wrapper.write(Types.TAG, TextUtil.stringToNbt(text)); // text
                        break;
                    case Times:
                        wrapper.setPacketType(ClientboundPackets1_21.SET_TITLES_ANIMATION);
                        wrapper.write(Types.INT, fadeInTicks); // fade in ticks
                        wrapper.write(Types.INT, stayTicks); // stay ticks
                        wrapper.write(Types.INT, fadeOutTicks); // fade out ticks
                        break;
                    default:
                        throw new IllegalStateException("Unhandled SetTitlePacket_TitleType: " + type);
                }
            } catch (Throwable e) { // Mojang client silently ignores errors
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Error while translating '" + originalText + "'", e);
                wrapper.cancel();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DISPLAY_OBJECTIVE, ClientboundPackets1_21.SET_DISPLAY_OBJECTIVE, wrapper -> {
            final ScoreboardTracker scoreboardTracker = wrapper.user().get(ScoreboardTracker.class);

            final String displaySlot = wrapper.read(BedrockTypes.STRING); // display slot
            final String objectiveName = wrapper.read(BedrockTypes.STRING); // objective name
            final String displayName = wrapper.read(BedrockTypes.STRING); // display name
            wrapper.read(BedrockTypes.STRING); // criteria
            final ObjectiveSortOrder sortOrder = ObjectiveSortOrder.getByValue(wrapper.read(BedrockTypes.VAR_INT), ObjectiveSortOrder.Descending); // sort order | Any invalid value is treated as no sorting, but Java Edition doesn't support that

            switch (displaySlot) {
                case "sidebar":
                    wrapper.write(Types.VAR_INT, 1); // position
                    break;
                case "belowname":
                    wrapper.write(Types.VAR_INT, 2); // position
                    break;
                case "list":
                    wrapper.write(Types.VAR_INT, 0); // position
                    break;
                default:
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock scoreboard display slot: " + displaySlot);
                    wrapper.cancel();
                    return;
            }
            wrapper.write(Types.STRING, objectiveName); // objective name

            if (objectiveName.isEmpty()) return;

            if (!scoreboardTracker.hasObjective(objectiveName)) {
                scoreboardTracker.addObjective(objectiveName, new ScoreboardObjective(objectiveName, sortOrder));

                final PacketWrapper scoreboardObjective = PacketWrapper.create(ClientboundPackets1_21.SET_OBJECTIVE, wrapper.user());
                scoreboardObjective.write(Types.STRING, objectiveName); // objective name
                scoreboardObjective.write(Types.BYTE, (byte) ScoreboardObjectiveAction.ADD.ordinal()); // mode
                scoreboardObjective.write(Types.TAG, TextUtil.stringToNbt(wrapper.user().get(ResourcePacksStorage.class).translate(displayName))); // display name
                scoreboardObjective.write(Types.VAR_INT, ObjectiveCriteriaRenderType.INTEGER.ordinal()); // display mode
                scoreboardObjective.write(Types.BOOLEAN, false); // has number format
                scoreboardObjective.send(BedrockProtocol.class);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_SCORE, null, wrapper -> {
            wrapper.cancel();
            final ScoreboardTracker scoreboardTracker = wrapper.user().get(ScoreboardTracker.class);

            final byte rawAction = wrapper.read(Types.BYTE); // action
            final ScorePacketType action = ScorePacketType.getByValue(rawAction);
            if (action == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ScorePacketType: " + rawAction);
                return;
            }
            final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // count
            for (int i = 0; i < count; i++) {
                final long scoreboardId = wrapper.read(BedrockTypes.VAR_LONG); // scoreboard id
                final String objectiveName = wrapper.read(BedrockTypes.STRING); // objective name
                final int score = wrapper.read(BedrockTypes.INT_LE); // score

                final ScoreboardEntry entry;
                switch (action) {
                    case Change:
                        final byte rawType = wrapper.read(Types.BYTE); // type
                        final IdentityDefinition_Type type = IdentityDefinition_Type.getByValue(rawType, IdentityDefinition_Type.Invalid);
                        Long uniqueEntityId = null;
                        String fakePlayerName = null;
                        switch (type) {
                            case Player:
                            case Entity:
                                uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
                                break;
                            case FakePlayer:
                                fakePlayerName = wrapper.read(BedrockTypes.STRING); // fake player name
                                break;
                            case Invalid: // Mojang client disconnects if the type is not valid
                                throw new IllegalStateException("Invalid IdentityDefinition_Type: " + rawType);
                            default:
                                throw new IllegalStateException("Unhandled IdentityDefinition_Type: " + rawType);
                        }
                        entry = new ScoreboardEntry(score, type, uniqueEntityId, fakePlayerName);
                        break;
                    case Remove:
                        entry = null;
                        break;
                    default:
                        throw new IllegalStateException("Unhandled ScorePacketType: " + action);
                }

                final ScoreboardObjective objective = scoreboardTracker.getObjective(objectiveName);
                final Pair<ScoreboardObjective, ScoreboardEntry> existingEntry = scoreboardTracker.getEntry(scoreboardId);
                if (existingEntry != null) {
                    existingEntry.key().removeEntry(wrapper.user(), scoreboardId);
                    if (entry != null && objective != null) {
                        existingEntry.value().setScore(entry.score());
                        objective.addEntry(wrapper.user(), scoreboardId, existingEntry.value());
                    }
                } else if (entry != null && objective != null) {
                    final ScoreboardEntry sameTargetEntry = objective.getEntryWithSameTarget(entry);
                    if (sameTargetEntry != null) {
                        sameTargetEntry.setScore(entry.score());
                        objective.updateEntryInPlace(wrapper.user(), sameTargetEntry);
                    } else if (entry.isValid()) {
                        objective.addEntry(wrapper.user(), scoreboardId, entry);
                    }
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_SCOREBOARD_IDENTITY, null, wrapper -> {
            wrapper.cancel();
            final ScoreboardTracker scoreboardTracker = wrapper.user().get(ScoreboardTracker.class);

            final byte rawAction = wrapper.read(Types.BYTE); // action
            final ScoreboardIdentityPacketType action = ScoreboardIdentityPacketType.getByValue(rawAction);
            if (action == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ScoreboardIdentityPacketType: " + rawAction);
                return;
            }
            final int count = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // count
            for (int i = 0; i < count; i++) {
                final long scoreboardId = wrapper.read(BedrockTypes.VAR_LONG); // scoreboard id
                final Pair<ScoreboardObjective, ScoreboardEntry> entry = scoreboardTracker.getEntry(scoreboardId);
                switch (action) {
                    case Update: {
                        final long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
                        if (entry == null) continue;
                        final ScoreboardEntry scoreboardEntry = entry.value();

                        if (scoreboardEntry.uniqueEntityId() == null) {
                            scoreboardEntry.updateTarget(IdentityDefinition_Type.Player, uniqueEntityId, scoreboardEntry.fakePlayerName());
                            entry.key().updateEntry(wrapper.user(), scoreboardEntry);
                        }
                        break;
                    }
                    case Remove: {
                        if (entry == null) continue;
                        final ScoreboardEntry scoreboardEntry = entry.value();

                        if (scoreboardEntry.fakePlayerName() != null) {
                            scoreboardEntry.updateTarget(IdentityDefinition_Type.FakePlayer, null, scoreboardEntry.fakePlayerName());
                            entry.key().updateEntry(wrapper.user(), scoreboardEntry);
                        }
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unhandled ScoreboardIdentityPacketType: " + action);
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.REMOVE_OBJECTIVE, ClientboundPackets1_21.SET_OBJECTIVE, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.STRING, Types.STRING); // objective name
                create(Types.BYTE, (byte) ScoreboardObjectiveAction.REMOVE.ordinal()); // mode
                handler(wrapper -> wrapper.user().get(ScoreboardTracker.class).removeObjective(wrapper.get(Types.STRING, 0)));
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.DEATH_INFO, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final String message = wrapper.read(BedrockTypes.STRING); // death cause message
            final String[] parameters = wrapper.read(BedrockTypes.STRING_ARRAY); // parameters

            final Function<String, String> translator = wrapper.user().get(ResourcePacksStorage.class).getTranslationLookup();
            gameSession.setDeathMessage(TextUtil.stringToTextComponent(BedrockTranslator.translate(message, translator, parameters)));
            // TODO: Respawn: If player is dead, reopen death screen to show the message
        });
    }

}
