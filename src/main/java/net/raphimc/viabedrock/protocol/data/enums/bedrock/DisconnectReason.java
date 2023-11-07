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
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

// File from CloudburstMC Protocol: https://github.com/CloudburstMC/Protocol/blob/bb8e4067b0d21826b7e6910dced986c428a8620f/bedrock-codec/src/main/java/org/cloudburstmc/protocol/bedrock/data/DisconnectFailReason.java
// under the Apache 2.0 license: https://github.com/CloudburstMC/Protocol/blob/bb8e4067b0d21826b7e6910dced986c428a8620f/LICENSE
public enum DisconnectReason {

    UNKNOWN,
    CANT_CONNECT_NO_INTERNET,
    NO_PERMISSIONS,
    UNRECOVERABLE_ERROR,
    THIRD_PARTY_BLOCKED,
    THIRD_PARTY_NO_INTERNET,
    THIRD_PARTY_BAD_IP,
    THIRD_PARTY_NO_SERVER_OR_SERVER_LOCKED,
    VERSION_MISMATCH,
    SKIN_ISSUE,
    INVITE_SESSION_NOT_FOUND,
    EDU_LEVEL_SETTINGS_MISSING,
    LOCAL_SERVER_NOT_FOUND,
    LEGACY_DISCONNECT,
    USER_LEAVE_GAME_ATTEMPTED,
    PLATFORM_LOCKED_SKINS_ERROR,
    REALMS_WORLD_UNASSIGNED,
    REALMS_SERVER_CANT_CONNECT,
    REALMS_SERVER_HIDDEN,
    REALMS_SERVER_DISABLED_BETA,
    REALMS_SERVER_DISABLED,
    CROSS_PLATFORM_DISALLOWED,
    CANT_CONNECT,
    SESSION_NOT_FOUND,
    CLIENT_SETTINGS_INCOMPATIBLE_WITH_SERVER,
    SERVER_FULL,
    INVALID_PLATFORM_SKIN,
    EDITION_VERSION_MISMATCH,
    EDITION_MISMATCH,
    LEVEL_NEWER_THAN_EXE_VERSION,
    NO_FAIL_OCCURRED,
    BANNED_SKIN,
    TIMEOUT,
    SERVER_NOT_FOUND,
    OUTDATED_SERVER,
    OUTDATED_CLIENT,
    NO_PREMIUM_PLATFORM,
    MULTIPLAYER_DISABLED,
    NO_WIFI,
    WORLD_CORRUPTION,
    NO_REASON,
    DISCONNECTED,
    INVALID_PLAYER,
    LOGGED_IN_OTHER_LOCATION,
    SERVER_ID_CONFLICT,
    NOT_ALLOWED,
    NOT_AUTHENTICATED,
    INVALID_TENANT,
    UNKNOWN_PACKET,
    UNEXPECTED_PACKET,
    INVALID_COMMAND_REQUEST_PACKET,
    HOST_SUSPENDED,
    LOGIN_PACKET_NO_REQUEST,
    LOGIN_PACKET_NO_CERT,
    MISSING_CLIENT,
    KICKED,
    KICKED_FOR_EXPLOIT,
    KICKED_FOR_IDLE,
    RESOURCE_PACK_PROBLEM,
    INCOMPATIBLE_PACK,
    OUT_OF_STORAGE,
    INVALID_LEVEL,
    DISCONNECT_PACKET_DEPRECATED,
    BLOCK_MISMATCH,
    INVALID_HEIGHTS,
    INVALID_WIDTHS,
    CONNECTION_LOST,
    ZOMBIE_CONNECTION,
    SHUTDOWN,
    REASON_NOT_SET,
    LOADING_STATE_TIMEOUT,
    RESOURCE_PACK_LOADING_FAILED,
    SEARCHING_FOR_SESSION_LOADING_SCREEN_FAILED,
    CONN_PROTOCOL_VERSION,
    SUBSYSTEM_STATUS_ERROR,
    EMPTY_AUTH_FROM_DISCOVERY,
    EMPTY_URL_FROM_DISCOVERY,
    EXPIRED_AUTH_FROM_DISCOVERY,
    UNKNOWN_SIGNAL_SERVICE_SIGN_IN_FAILURE,
    XBL_JOIN_LOBBY_FAILURE,
    UNSPECIFIED_CLIENT_INSTANCE_DISCONNECTION,
    CONN_SESSION_NOT_FOUND,
    CONN_CREATE_PEER_CONNECTION,
    CONN_ICE,
    CONN_CONNECT_REQUEST,
    CONN_CONNECT_RESPONSE,
    CONN_NEGOTIATION_TIMEOUT,
    CONN_INACTIVITY_TIMEOUT,
    STALE_CONNECTION_BEING_REPLACED,
    REALMS_SESSION_NOT_FOUND,
    BAD_PACKET;

    public static DisconnectReason fromId(final int id) {
        return id >= 0 && id < values().length ? values()[id] : UNKNOWN;
    }

}
