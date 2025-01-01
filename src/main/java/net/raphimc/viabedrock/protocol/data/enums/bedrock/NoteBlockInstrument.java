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
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

import net.raphimc.viabedrock.protocol.BedrockProtocol;

public enum NoteBlockInstrument {

    HARP("note.harp"),
    BASS_DRUM("note.bd"),
    SNARE("note.snare"),
    HAT("note.hat"),
    BASS("note.bass"),
    BELL("note.bell"),
    FLUTE("note.flute"),
    CHIME("note.chime"),
    GUITAR("note.guitar"),
    XYLOPHONE("note.xylophone"),
    IRON_XYLOPHONE("note.iron_xylophone"),
    COW_BELL("note.cow_bell"),
    DIDGERIDOO("note.didgeridoo"),
    BIT("note.bit"),
    BANJO("note.banjo"),
    PLING("note.pling"),
    SKELETON("note.skeleton"),
    WITHER_SKELETON("note.witherskeleton"),
    ZOMBIE("note.zombie"),
    CREEPER("note.creeper"),
    ENDER_DRAGON("note.enderdragon"),
    PIGLIN("note.piglin");

    public static NoteBlockInstrument getByValue(final int value) {
        if (value < 0 || value >= values().length) {
            return HARP;
        }
        return values()[value];
    }

    private final String soundName;

    NoteBlockInstrument(final String soundName) {
        if (!BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().containsKey(soundName)) {
            throw new IllegalArgumentException("Unknown bedrock sound name: " + soundName);
        }
        this.soundName = soundName;
    }

    public String soundName() {
        return this.soundName;
    }

}
