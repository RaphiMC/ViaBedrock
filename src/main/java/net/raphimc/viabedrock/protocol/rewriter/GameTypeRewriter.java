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
package net.raphimc.viabedrock.protocol.rewriter;

public class GameTypeRewriter {

    public static short gameTypeToGameMode(final int gameType) {
        switch (gameType) {
            case 0: // SURVIVAL
            case 3: // SURVIVAL_VIEWER
            case 4: // CREATIVE_VIEWER
            default: // Mojang client defaults to survival in case of out of bounds values
                return 0;
            case 1: // CREATIVE
                return 1;
            case 2: // ADVENTURE
                return 2;
            case 5: // DEFAULT
                return -1;
            case 6: // SPECTATOR
                return 3;
        }
    }

    public static short getEffectiveGameMode(final int playerGameType, final int levelGameType) {
        short effectiveGameMode = gameTypeToGameMode(playerGameType);
        if (effectiveGameMode == -1) {
            effectiveGameMode = gameTypeToGameMode(levelGameType);
        }
        if (effectiveGameMode == -1) {
            effectiveGameMode = 0; // Mojang client defaults to survival in case of out of bounds values
        }
        return effectiveGameMode;
    }

}
