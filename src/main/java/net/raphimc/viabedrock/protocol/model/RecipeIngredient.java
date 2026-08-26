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
package net.raphimc.viabedrock.protocol.model;

/**
 * One input of a crafting recipe. Bedrock describes inputs in several ways, not all of which can be resolved to a
 * concrete set of items.
 *
 * @param type       The way this ingredient is described
 * @param intId      The runtime item id, only set for {@link Type#INT_ID_META}
 * @param identifier The item identifier, only set for {@link Type#STRING_ID_META}
 * @param tag        The item tag, only set for {@link Type#TAG}
 * @param meta       The item meta, or {@link #WILDCARD_META} if any meta matches
 * @param count      How many items of this kind the recipe consumes
 */
public record RecipeIngredient(Type type, int intId, String identifier, String tag, int meta, int count) {

    public static final int WILDCARD_META = 0x7FFF;

    public static final RecipeIngredient EMPTY = new RecipeIngredient(Type.EMPTY, 0, null, null, 0, 0);

    public enum Type {

        /**
         * An empty slot.
         */
        EMPTY,
        /**
         * A specific item, identified by its runtime id.
         */
        INT_ID_META,
        /**
         * A Molang expression. Can't be evaluated, so it matches anything.
         */
        MOLANG,
        /**
         * Any item with the given item tag.
         */
        TAG,
        /**
         * A specific item, identified by its identifier.
         */
        STRING_ID_META,
        /**
         * An alias for a group of items. Can't be resolved, so it matches anything.
         */
        COMPLEX_ALIAS,
        ;

    }

    public boolean isEmpty() {
        return this.type == Type.EMPTY;
    }

}
