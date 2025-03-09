
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
package net.raphimc.viabedrock.api.util.mocha;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorFlags;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.standard.MochaMath;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorFlags.*;

public final class MochaUtil {
    // Hardcoded...
    private static final Map<ActorFlags, List<String>> UNIQUE_FLAG_QUERY_NAME = Map.ofEntries(
            Map.entry(ONFIRE, List.of("is_on_fire", "is_onfire")),
            Map.entry(USINGITEM, List.of("is_using_item")),
            Map.entry(DAMAGENEARBYMOBS, List.of("can_damage_nearby_mobs")),
            Map.entry(WALLCLIMBING, List.of("is_wall_climbing")),
            Map.entry(INLOVE, List.of("is_in_love")),
            Map.entry(CANSWIM, List.of("can_swim")),
            Map.entry(CANFLY, List.of("can_fly")),
            Map.entry(CANWALK, List.of("can_walk")),
            Map.entry(RAM_ATTACK, List.of("is_ram_attacking")),
            Map.entry(DELAYED_ATTACK, List.of("is_delayed_attacking"))
    );

    @SuppressWarnings("UnstableApiUsage")
    public static SimpleMochaEngine<?> build(final ResourcePacksStorage storage, final BedrockEntityData bedrock, final EntityData[] entityData, final Set<ActorFlags> flags) {
        final Scope.Builder builder = Scope.builder();
        builder.set("math", JavaObjectBinding.of(MochaMath.class, null, new MochaMath()));

        final MutableObjectBinding variable = new MutableObjectBinding();
        builder.set("variable", variable);
        builder.set("v", variable);

        final MutableObjectBinding query = new MutableObjectBinding();

        for (final EntityData data : entityData) {
            final ActorDataIDs dataID = ActorDataIDs.getByValue(data.id());
            if (dataID == null || dataID == ActorDataIDs.RESERVED_0) {
                continue;
            }

            if (!(data.value() instanceof Number) && !(data.value() instanceof Boolean)) {
                continue;
            }

            final Object value = data.value();
            query.set(dataID.name().toLowerCase(), Value.of(value));
        }

        for (final ActorFlags flag : ActorFlags.values()) {
            final @NotNull Value state = Value.of(flags.contains(flag));

            if (UNIQUE_FLAG_QUERY_NAME.containsKey(flag)) {
                for (final String queryName : UNIQUE_FLAG_QUERY_NAME.get(flag)) {
                    query.set(queryName, state);
                }
                continue;
            }

            final String loweredCaseName = flag.name();
            if (loweredCaseName.startsWith("can_") || loweredCaseName.startsWith("in_") || loweredCaseName.startsWith("is_") || loweredCaseName.startsWith("has_")) {
                query.set(loweredCaseName, state);
            } else {
                query.set("is_" + loweredCaseName, state);
            }
        }

        builder.set("query", query);
        builder.set("q", query);

        final MutableObjectBinding texture = new MutableObjectBinding();
        for (final Map.Entry<String, String> entry : bedrock.getTextures().entrySet()) {
            texture.set(entry.getKey(), Value.of(entry.getKey()));
        }

        builder.set("texture", texture);

        final MutableObjectBinding geometry = new MutableObjectBinding();
        for (final Map.Entry<String, String> entry : bedrock.getGeometries().entrySet()) {
            geometry.set(entry.getKey(), Value.of(entry.getKey()));
        }

        builder.set("geometry", geometry);

        final MutableObjectBinding array = new MutableObjectBinding();

        for (final BedrockEntityData.RenderController controllerName : bedrock.getControllers()) {
            final BedrockRenderController controller = storage.getRenderControllers().controllers().get(controllerName.getIdentifier());
            if (controller == null) {
                continue;
            }

            for (final Map.Entry<String, List<String>> entry : controller.getArrays().entrySet()) {
                final List<String> values = new ArrayList<>();
                for (final String value : entry.getValue()) {
                    // This attempted to redirect to another array, I don't know what this for yet since it's not used anywhere.
                    if (value.toLowerCase().startsWith("array.")) {
                        continue;
                    }

                    values.add(value);
                }

                if (!values.isEmpty()) {
                    String name = entry.getKey();
                    if (name.toLowerCase().startsWith("array.")) {
                        name = name.toLowerCase().replace("array.", "");
                    }

                    array.set(name, Value.of(values.toArray()));
                }
            }
        }

        builder.set("array", array);
        return new SimpleMochaEngine<>(builder.build());
    }
}
