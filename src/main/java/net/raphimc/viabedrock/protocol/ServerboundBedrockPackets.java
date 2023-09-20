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
package net.raphimc.viabedrock.protocol;

import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;

public enum ServerboundBedrockPackets implements ServerboundPacketType {

    LOGIN(1),
    PLAY_STATUS(2),
    SERVER_TO_CLIENT_HANDSHAKE(3),
    CLIENT_TO_SERVER_HANDSHAKE(4),
    DISCONNECT(5),
    RESOURCE_PACKS_INFO(6),
    RESOURCE_PACK_STACK(7),
    RESOURCE_PACK_CLIENT_RESPONSE(8),
    TEXT(9),
    SET_TIME(10),
    START_GAME(11),
    ADD_PLAYER(12),
    ADD_ENTITY(13),
    REMOVE_ENTITY(14),
    ADD_ITEM_ENTITY(15),
    TAKE_ITEM_ENTITY(17),
    MOVE_ENTITY_ABSOLUTE(18),
    MOVE_PLAYER(19),
    RIDER_JUMP(20),
    UPDATE_BLOCK(21),
    ADD_PAINTING(22),
    TICK_SYNC(23),
    LEVEL_SOUND_EVENT_V1(24),
    LEVEL_EVENT(25),
    BLOCK_EVENT(26),
    ENTITY_EVENT(27),
    MOB_EFFECT(28),
    UPDATE_ATTRIBUTES(29),
    INVENTORY_TRANSACTION(30),
    MOB_EQUIPMENT(31),
    MOB_ARMOR_EQUIPMENT(32),
    INTERACT(33),
    BLOCK_PICK_REQUEST(34),
    ENTITY_PICK_REQUEST(35),
    PLAYER_ACTION(36),
    HURT_ARMOR(38),
    SET_ENTITY_DATA(39),
    SET_ENTITY_MOTION(40),
    SET_ENTITY_LINK(41),
    SET_HEALTH(42),
    SET_SPAWN_POSITION(43),
    ANIMATE(44),
    RESPAWN(45),
    CONTAINER_OPEN(46),
    CONTAINER_CLOSE(47),
    PLAYER_HOTBAR(48),
    INVENTORY_CONTENT(49),
    INVENTORY_SLOT(50),
    CONTAINER_SET_DATA(51),
    CRAFTING_DATA(52),
    CRAFTING_EVENT(53),
    GUI_DATA_PICK_ITEM(54),
    BLOCK_ENTITY_DATA(56),
    PLAYER_INPUT(57),
    LEVEL_CHUNK(58),
    SET_COMMANDS_ENABLED(59),
    SET_DIFFICULTY(60),
    CHANGE_DIMENSION(61),
    SET_PLAYER_GAME_TYPE(62),
    PLAYER_LIST(63),
    SIMPLE_EVENT(64),
    LEGACY_TELEMETRY_EVENT(65),
    SPAWN_EXPERIENCE_ORB(66),
    CLIENTBOUND_MAP_ITEM_DATA(67),
    MAP_INFO_REQUEST(68),
    REQUEST_CHUNK_RADIUS(69),
    CHUNK_RADIUS_UPDATED(70),
    ITEM_FRAME_DROP_ITEM(71),
    GAME_RULES_CHANGED(72),
    CAMERA(73),
    BOSS_EVENT(74),
    SHOW_CREDITS(75),
    AVAILABLE_COMMANDS(76),
    COMMAND_REQUEST(77),
    COMMAND_BLOCK_UPDATE(78),
    COMMAND_OUTPUT(79),
    UPDATE_TRADE(80),
    UPDATE_EQUIP(81),
    RESOURCE_PACK_DATA_INFO(82),
    RESOURCE_PACK_CHUNK_DATA(83),
    RESOURCE_PACK_CHUNK_REQUEST(84),
    TRANSFER(85),
    PLAY_SOUND(86),
    STOP_SOUND(87),
    SET_TITLE(88),
    ADD_BEHAVIOR_TREE(89),
    STRUCTURE_BLOCK_UPDATE(90),
    SHOW_STORE_OFFER(91),
    PURCHASE_RECEIPT(92),
    PLAYER_SKIN(93),
    SUB_CLIENT_LOGIN(94),
    AUTOMATION_CLIENT_CONNECT(95),
    SET_LAST_HURT_BY(96),
    BOOK_EDIT(97),
    NPC_REQUEST(98),
    PHOTO_TRANSFER(99),
    MODAL_FORM_REQUEST(100),
    MODAL_FORM_RESPONSE(101),
    SERVER_SETTINGS_REQUEST(102),
    SERVER_SETTINGS_RESPONSE(103),
    SHOW_PROFILE(104),
    SET_DEFAULT_GAME_TYPE(105),
    REMOVE_OBJECTIVE(106),
    SET_DISPLAY_OBJECTIVE(107),
    SET_SCORE(108),
    LAB_TABLE(109),
    UPDATE_BLOCK_SYNCED(110),
    MOVE_ENTITY_DELTA(111),
    SET_SCOREBOARD_IDENTITY(112),
    SET_LOCAL_PLAYER_AS_INITIALIZED(113),
    UPDATE_SOFT_ENUM(114),
    NETWORK_STACK_LATENCY(115),
    SPAWN_PARTICLE_EFFECT(118),
    AVAILABLE_ENTITY_IDENTIFIERS(119),
    LEVEL_SOUND_EVENT_V2(120),
    NETWORK_CHUNK_PUBLISHER_UPDATE(121),
    BIOME_DEFINITION_LIST(122),
    LEVEL_SOUND_EVENT(123),
    LEVEL_EVENT_GENERIC(124),
    LECTERN_UPDATE(125),
    VIDEO_STREAM_CONNECT(126),
    ADD_ECS_ENTITY(127),
    REMOVE_ECS_ENTITY(128),
    CLIENT_CACHE_STATUS(129),
    ON_SCREEN_TEXTURE_ANIMATION(130),
    MAP_CREATE_LOCKED_COPY(131),
    STRUCTURE_TEMPLATE_DATA_REQUEST(132),
    STRUCTURE_TEMPLATE_DATA_RESPONSE(133),
    CLIENT_CACHE_BLOB_STATUS(135),
    CLIENT_CACHE_MISS_RESPONSE(136),
    EDUCATION_SETTINGS(137),
    EMOTE(138),
    MULTIPLAYER_SETTINGS(139),
    SETTINGS_COMMAND(140),
    ANVIL_DAMAGE(141),
    COMPLETED_USING_ITEM(142),
    NETWORK_SETTINGS(143),
    PLAYER_AUTH_INPUT(144),
    CREATIVE_CONTENT(145),
    PLAYER_ENCHANT_OPTIONS(146),
    ITEM_STACK_REQUEST(147),
    ITEM_STACK_RESPONSE(148),
    PLAYER_ARMOR_DAMAGE(149),
    CODE_BUILDER(150),
    UPDATE_PLAYER_GAME_TYPE(151),
    EMOTE_LIST(152),
    POSITION_TRACKING_DB_SERVER_BROADCAST(153),
    POSITION_TRACKING_DB_CLIENT_REQUEST(154),
    DEBUG_INFO(155),
    PACKET_VIOLATION_WARNING(156),
    MOTION_PREDICTION_HINTS(157),
    ANIMATE_ENTITY(158),
    CAMERA_SHAKE(159),
    PLAYER_FOG(160),
    CORRECT_PLAYER_MOVE_PREDICTION(161),
    ITEM_COMPONENT(162),
    FILTER_TEXT(163),
    CLIENTBOUND_DEBUG_RENDERER(164),
    SYNC_ENTITY_PROPERTY(165),
    ADD_VOLUME_ENTITY(166),
    REMOVE_VOLUME_ENTITY(167),
    SIMULATION_TYPE(168),
    NPC_DIALOGUE(169),
    EDU_URI_RESOURCE(170),
    CREATE_PHOTO(171),
    UPDATE_SUB_CHUNK_BLOCKS(172),
    PHOTO_INFO_REQUEST(173),
    SUB_CHUNK(174),
    SUB_CHUNK_REQUEST(175),
    PLAYER_START_ITEM_COOLDOWN(176),
    SCRIPT_MESSAGE(177),
    CODE_BUILDER_SOURCE(178),
    TICKING_AREAS_LOAD_STATUS(179),
    DIMENSION_DATA(180),
    AGENT_ACTION_EVENT(181),
    CHANGE_MOB_PROPERTY(182),
    LESSON_PROGRESS(183),
    REQUEST_ABILITY(184),
    REQUEST_PERMISSIONS(185),
    TOAST_REQUEST(186),
    UPDATE_ABILITIES(187),
    UPDATE_ADVENTURE_SETTINGS(188),
    DEATH_INFO(189),
    EDITOR_NETWORK(190),
    FEATURE_REGISTRY(191),
    SERVER_STATS(192),
    REQUEST_NETWORK_SETTINGS(193),
    GAME_TEST_REQUEST(194),
    GAME_TEST_RESULTS(195),
    UPDATE_CLIENT_INPUT_LOCKS(196),
    CAMERA_PRESETS(198),
    UNLOCKED_RECIPES(199),
    CAMERA_INSTRUCTION(300),
    COMPRESSED_BIOME_DEFINITION_LIST(301),
    TRIM_DATA(302),
    OPEN_SIGN(303),
    AGENT_ANIMATION(304),
    REFRESH_ENTITLEMENTS(305);

    private static final ServerboundBedrockPackets[] REGISTRY = new ServerboundBedrockPackets[512];

    static {
        for (ServerboundBedrockPackets packet : values()) {
            REGISTRY[packet.id] = packet;
        }
    }

    public static ServerboundBedrockPackets getPacket(final int id) {
        if (id < 0 || id >= REGISTRY.length) return null;

        return REGISTRY[id];
    }

    ServerboundBedrockPackets(final int id) {
        this.id = id;
    }

    private final int id;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return name();
    }

}
