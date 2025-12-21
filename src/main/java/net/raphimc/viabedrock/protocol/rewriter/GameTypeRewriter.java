/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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

import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.GameType;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.GameMode;

public class GameTypeRewriter {

    public static GameMode getEffectiveGameMode(final GameType playerGameType, final GameType levelGameType) {
        GameType effectiveGameType = playerGameType;
        if (effectiveGameType == GameType.Undefined || effectiveGameType == GameType.Default) {
            effectiveGameType = levelGameType;
        }
        if (effectiveGameType == GameType.Undefined || effectiveGameType == GameType.Default) {
            effectiveGameType = GameType.Survival; // Bedrock client defaults to survival in case of out of bounds values
        }
        return (switch (effectiveGameType) {
            case Survival -> GameMode.SURVIVAL;
            case Creative -> GameMode.CREATIVE;
            case Adventure -> GameMode.ADVENTURE;
            case Spectator -> GameMode.SPECTATOR;
            default -> throw new IllegalStateException("Unhandled GameType: " + effectiveGameType);
        });
    }

}
