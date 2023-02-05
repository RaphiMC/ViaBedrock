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

import net.raphimc.viabedrock.protocol.data.enums.bedrock.GameTypes;

public class GameTypeRewriter {

    public static short gameTypeToGameMode(final int gameType) {
        switch (gameType) {
            case GameTypes.SURVIVAL:
            case GameTypes.SURVIVAL_VIEWER:
            case GameTypes.CREATIVE_VIEWER:
            default: // Mojang client defaults to survival in case of out of bounds values
                return 0;
            case GameTypes.CREATIVE: // CREATIVE
                return 1;
            case GameTypes.ADVENTURE: // ADVENTURE
                return 2;
            case GameTypes.DEFAULT: // DEFAULT
                return -1;
            case GameTypes.SPECTATOR: // SPECTATOR
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
