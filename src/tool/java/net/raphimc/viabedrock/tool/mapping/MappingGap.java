/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.tool.mapping;

/**
 * One problem found in the data assets.
 *
 * @param category The mapping category, for example {@code block_states}
 * @param kind     What is wrong with it
 * @param key      The bedrock identifier or block state the problem belongs to
 * @param detail   Extra context, for example the java identifier which no longer exists
 */
public record MappingGap(String category, Kind kind, String key, String detail) {

    public enum Kind {

        /**
         * Bedrock has the identifier but the mapping file doesn't.
         */
        MISSING,
        /**
         * The mapping file has an identifier which bedrock no longer has.
         */
        STALE,
        /**
         * The mapping exists but points at something java doesn't have, or has the wrong shape.
         */
        BROKEN

    }

    @Override
    public String toString() {
        return this.kind + " " + this.category + " " + this.key + (this.detail == null || this.detail.isEmpty() ? "" : " (" + this.detail + ")");
    }

}
