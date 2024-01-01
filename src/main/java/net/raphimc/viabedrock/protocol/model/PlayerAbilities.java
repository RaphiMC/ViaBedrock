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
package net.raphimc.viabedrock.protocol.model;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;

import java.util.Objects;

public class PlayerAbilities {

    private final long uniqueEntityId;
    private final int playerPermission;
    private final int commandPermission;
    private final Int2ObjectMap<Abilities> abilityLayers;

    public PlayerAbilities(final long uniqueEntityId, final int playerPermission, final int commandPermission, final Int2ObjectMap<Abilities> abilityLayers) {
        this.uniqueEntityId = uniqueEntityId;
        this.playerPermission = playerPermission;
        this.commandPermission = commandPermission;
        this.abilityLayers = abilityLayers;
    }

    public long uniqueEntityId() {
        return this.uniqueEntityId;
    }

    public int playerPermission() {
        return this.playerPermission;
    }

    public int commandPermission() {
        return this.commandPermission;
    }

    public Int2ObjectMap<Abilities> abilityLayers() {
        return this.abilityLayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAbilities that = (PlayerAbilities) o;
        return uniqueEntityId == that.uniqueEntityId && playerPermission == that.playerPermission && commandPermission == that.commandPermission && Objects.equals(abilityLayers, that.abilityLayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueEntityId, playerPermission, commandPermission, abilityLayers);
    }

    @Override
    public String toString() {
        return "PlayerAbilities{" +
                "uniqueEntityId=" + uniqueEntityId +
                ", playerPermission=" + playerPermission +
                ", commandPermission=" + commandPermission +
                ", abilityLayers=" + abilityLayers +
                '}';
    }

    public static class Abilities {

        private final int abilitiesSet;
        private final int abilityValues;
        private final float walkSpeed;
        private final float flySpeed;

        public Abilities(final int abilitiesSet, final int abilityValues, final float walkSpeed, final float flySpeed) {
            this.abilitiesSet = abilitiesSet;
            this.abilityValues = abilityValues;
            this.walkSpeed = walkSpeed;
            this.flySpeed = flySpeed;
        }

        public int abilitiesSet() {
            return this.abilitiesSet;
        }

        public int abilityValues() {
            return this.abilityValues;
        }

        public float walkSpeed() {
            return this.walkSpeed;
        }

        public float flySpeed() {
            return this.flySpeed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Abilities abilities = (Abilities) o;
            return abilitiesSet == abilities.abilitiesSet && abilityValues == abilities.abilityValues && Float.compare(abilities.walkSpeed, walkSpeed) == 0 && Float.compare(abilities.flySpeed, flySpeed) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(abilitiesSet, abilityValues, walkSpeed, flySpeed);
        }

        @Override
        public String toString() {
            return "Abilities{" +
                    "abilitiesSet=" + abilitiesSet +
                    ", abilityValues=" + abilityValues +
                    ", walkSpeed=" + walkSpeed +
                    ", flySpeed=" + flySpeed +
                    '}';
        }

    }

}
