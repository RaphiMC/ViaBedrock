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

public class PlayStatus {

    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_FAILED_CLIENT_OLD = 1;
    public static final int LOGIN_FAILED_SERVER_OLD = 2;
    public static final int PLAYER_SPAWN = 3;
    public static final int LOGIN_FAILED_INVALID_TENANT = 4;
    public static final int LOGIN_FAILED_EDITION_MISMATCH_EDU_TO_VANILLA = 5;
    public static final int LOGIN_FAILED_EDITION_MISMATCH_VANILLA_TO_EDU = 6;
    public static final int FAILED_SERVER_FULL_SUB_CLIENT = 7;
    public static final int EDITOR_TO_VANILLA_MISMATCH = 8;
    public static final int VANILLA_TO_EDITOR_MISMATCH = 9;

}
