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

public class SubChunkResults {

    public static final byte UNDEFINED = 0;
    public static final byte SUCCESS = 1;
    public static final byte CHUNK_NOT_FOUND = 2;
    public static final byte INVALID_DIMENSION = 3;
    public static final byte PLAYER_NOT_FOUND = 4;
    public static final byte INDEX_OUT_OF_BOUNDS = 5;
    public static final byte SUCCESS_ALL_AIR = 6;

}
