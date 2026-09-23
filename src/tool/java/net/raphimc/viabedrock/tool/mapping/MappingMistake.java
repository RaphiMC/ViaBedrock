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
 * A mapping which loads fine but looks wrong.
 *
 * @param category The mapping category the finding belongs to
 * @param check    Which check produced it
 * @param key      The bedrock identifier or block state
 * @param detail   What looks wrong and what the rest of the data suggests instead
 */
public record MappingMistake(String category, String check, String key, String detail) {

    public static final String IDENTITY_AVAILABLE = "identity-available";
    public static final String UNMAPPED_BUT_PRESENT = "unmapped-but-present";
    public static final String FAMILY_OUTLIER = "family-outlier";
    public static final String IGNORED_PROPERTY = "ignored-property";

}
