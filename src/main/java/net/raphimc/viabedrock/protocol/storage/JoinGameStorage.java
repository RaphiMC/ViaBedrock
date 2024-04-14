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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.Difficulty;

public class JoinGameStorage implements StorableObject {

    private final String levelName;
    private final Difficulty difficulty;
    private final float rainLevel;
    private final float lightningLevel;

    public JoinGameStorage(final String levelName, final Difficulty difficulty, final float rainLevel, final float lightningLevel) {
        this.levelName = levelName;
        this.difficulty = difficulty;
        this.rainLevel = rainLevel;
        this.lightningLevel = lightningLevel;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public float getRainLevel() {
        return this.rainLevel;
    }

    public float getLightningLevel() {
        return this.lightningLevel;
    }

}
