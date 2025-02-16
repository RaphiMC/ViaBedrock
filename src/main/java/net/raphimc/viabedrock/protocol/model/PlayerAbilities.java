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
package net.raphimc.viabedrock.protocol.model;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.AbilitiesIndex;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.SerializedAbilitiesData_SerializedAbilitiesLayer;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public record PlayerAbilities(long uniqueEntityId, byte playerPermission, byte commandPermission, Map<SerializedAbilitiesData_SerializedAbilitiesLayer, AbilitiesLayer> abilityLayers) {

    public PlayerAbilities(final long uniqueEntityId, final byte playerPermission, final byte commandPermission) {
        this(uniqueEntityId, playerPermission, commandPermission, new EnumMap<>(SerializedAbilitiesData_SerializedAbilitiesLayer.class));

        final Set<AbilitiesIndex> abilitiesSet = EnumSet.allOf(AbilitiesIndex.class);
        abilitiesSet.remove(AbilitiesIndex.Invalid);
        this.abilityLayers.put(SerializedAbilitiesData_SerializedAbilitiesLayer.Base, new AbilitiesLayer(
                abilitiesSet,
                EnumSet.of(AbilitiesIndex.Build, AbilitiesIndex.Mine, AbilitiesIndex.DoorsAndSwitches, AbilitiesIndex.OpenContainers, AbilitiesIndex.AttackPlayers, AbilitiesIndex.AttackMobs),
                0.1F, 0.05F, 1F
        ));
    }

    public boolean getBooleanValue(final AbilitiesIndex ability) {
        for (SerializedAbilitiesData_SerializedAbilitiesLayer layer : SerializedAbilitiesData_SerializedAbilitiesLayer.values()) {
            final AbilitiesLayer abilitiesLayer = this.abilityLayers.get(layer);
            if (abilitiesLayer != null && abilitiesLayer.abilitiesSet().contains(ability)) {
                return abilitiesLayer.abilityValues().contains(ability);
            }
        }
        return false;
    }

    public float getFloatValue(final AbilitiesIndex ability) {
        for (SerializedAbilitiesData_SerializedAbilitiesLayer layer : SerializedAbilitiesData_SerializedAbilitiesLayer.values()) {
            final AbilitiesLayer abilitiesLayer = this.abilityLayers.get(layer);
            if (abilitiesLayer != null && abilitiesLayer.abilitiesSet().contains(ability)) {
                return switch (ability) {
                    case WalkSpeed -> abilitiesLayer.walkSpeed();
                    case FlySpeed -> abilitiesLayer.flySpeed();
                    case VerticalFlySpeed -> abilitiesLayer.verticalFlySpeed();
                    default -> throw new IllegalArgumentException("Ability " + ability + " is not a float value");
                };
            }
        }
        return 0F;
    }

    public AbilitiesLayer getOrCreateCacheLayer() {
        return this.abilityLayers.computeIfAbsent(SerializedAbilitiesData_SerializedAbilitiesLayer.CustomCache, layer -> new PlayerAbilities.AbilitiesLayer(EnumSet.noneOf(AbilitiesIndex.class), EnumSet.noneOf(AbilitiesIndex.class), 0F, 0F, 0F));
    }

    public record AbilitiesLayer(Set<AbilitiesIndex> abilitiesSet, Set<AbilitiesIndex> abilityValues, float walkSpeed, float flySpeed, float verticalFlySpeed) {

        public void setAbility(final AbilitiesIndex ability, final boolean value) {
            this.abilitiesSet.add(ability);
            if (value) {
                this.abilityValues.add(ability);
            } else {
                this.abilityValues.remove(ability);
            }
        }

    }

}
